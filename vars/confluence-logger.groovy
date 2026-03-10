/**
 * confluence-logger.groovy
 * ─────────────────────────────────────────────────────────────────
 * Shared-library helper that:
 *   1. Resolves (or creates) the correct Confluence page for a given
 *      beta pipeline + branch combination.
 *   2. Appends a structured log row (date, status, messages,
 *      initiated-by) to that page.
 *
 * USAGE  – call from any Multibranch or standard Pipeline Jenkinsfile:
 *
 *   @Library('your-shared-lib') _
 *   confluenceLogger.logBuildResult(this)
 *
 * REQUIRED JENKINS CREDENTIALS
 *   confluence-user-token  →  "Secret text" credential containing a
 *                             Confluence API token (Basic auth will be
 *                             constructed as  user:token encoded in b64)
 *
 * REQUIRED ENVIRONMENT VARIABLES (set in Jenkins or Jenkinsfile)
 *   CONFLUENCE_BASE_URL     e.g. https://yourcompany.atlassian.net/wiki
 *   CONFLUENCE_SPACE_KEY    e.g. DEVOPS
 *   CONFLUENCE_PARENT_PAGE_ID   numeric ID of the parent page that
 *                               groups all beta logs
 *   CONFLUENCE_USER         e.g. jenkins-bot@yourcompany.com
 * ─────────────────────────────────────────────────────────────────
 */

// ── public entry-point ────────────────────────────────────────────
def logBuildResult(script) {
    def ctx = buildContext(script)
    withCredentials([string(credentialsId: 'confluence-user-token',
                            variable: 'CONF_TOKEN')]) {
        def auth = buildAuthHeader(env.CONFLUENCE_USER, CONF_TOKEN)
        def pageId = resolveOrCreatePage(ctx, auth)
        appendLogRow(ctx, pageId, auth)
    }
}

// ── context builder ───────────────────────────────────────────────
def buildContext(script) {
    def isSuccess = (currentBuild.result == null ||
                     currentBuild.result == 'SUCCESS')

    // Collect error messages from the build log (last 80 lines)
    def rawLog    = currentBuild.rawBuild.getLog(80).join('\n')
    def errors    = extractErrors(rawLog)

    // Who triggered the build?
    def initiator = resolveInitiator()

    return [
        betaName    : env.JOB_BASE_NAME ?: env.JOB_NAME.tokenize('/').last(),
        branch      : env.BRANCH_NAME   ?: env.GIT_BRANCH ?: 'unknown',
        buildNumber : env.BUILD_NUMBER,
        buildUrl    : env.BUILD_URL,
        startTime   : new Date(currentBuild.startTimeInMillis)
                            .format("yyyy-MM-dd HH:mm:ss z"),
        status      : isSuccess ? 'SUCCESS' : (currentBuild.result ?: 'FAILURE'),
        errors      : errors,
        initiatedBy : initiator,
        spaceKey    : env.CONFLUENCE_SPACE_KEY,
        baseUrl     : env.CONFLUENCE_BASE_URL,
        parentPageId: env.CONFLUENCE_PARENT_PAGE_ID
    ]
}

// ── page resolution ───────────────────────────────────────────────
/**
 * Returns the Confluence page ID for  "<betaName> – <branch> Beta Logs".
 * Creates the page (with a header table) if it does not yet exist.
 */
def resolveOrCreatePage(ctx, auth) {
    def pageTitle = "${ctx.betaName} – ${ctx.branch} Beta Logs"
    def existingId = findPage(ctx.baseUrl, ctx.spaceKey, pageTitle, auth)

    if (existingId) {
        echo "Confluence page found: '${pageTitle}' (id=${existingId})"
        return existingId
    }

    echo "Confluence page not found – creating: '${pageTitle}'"
    return createPage(ctx, pageTitle, auth)
}

def findPage(baseUrl, spaceKey, title, auth) {
    def encodedTitle = java.net.URLEncoder.encode(title, 'UTF-8')
    def url = "${baseUrl}/rest/api/content" +
              "?spaceKey=${spaceKey}&title=${encodedTitle}&expand=version"

    def response = httpRequest(
        url              : url,
        httpMode         : 'GET',
        customHeaders    : [[name: 'Authorization', value: auth],
                            [name: 'Content-Type',  value: 'application/json']],
        validResponseCodes: '200'
    )

    def json = readJSON text: response.content
    if (json.results && json.results.size() > 0) {
        return json.results[0].id
    }
    return null
}

def createPage(ctx, pageTitle, auth) {
    def body = initialPageBody()
    def payload = groovy.json.JsonOutput.toJson([
        type     : 'page',
        title    : pageTitle,
        space    : [key: ctx.spaceKey],
        ancestors: [[id: ctx.parentPageId]],
        body     : [
            storage: [
                value         : body,
                representation: 'storage'
            ]
        ]
    ])

    def response = httpRequest(
        url              : "${ctx.baseUrl}/rest/api/content",
        httpMode         : 'POST',
        customHeaders    : [[name: 'Authorization', value: auth],
                            [name: 'Content-Type',  value: 'application/json']],
        requestBody      : payload,
        validResponseCodes: '200'
    )

    def json = readJSON text: response.content
    echo "Created Confluence page id=${json.id}"
    return json.id
}

// ── log-row appender ──────────────────────────────────────────────
/**
 * Fetches current page content, injects a new <tr> row, then PUTs it back.
 */
def appendLogRow(ctx, pageId, auth) {
    // 1. GET current page
    def getResp = httpRequest(
        url              : "${ctx.baseUrl}/rest/api/content/${pageId}" +
                           "?expand=body.storage,version",
        httpMode         : 'GET',
        customHeaders    : [[name: 'Authorization', value: auth],
                            [name: 'Content-Type',  value: 'application/json']],
        validResponseCodes: '200'
    )
    def pageJson = readJSON text: getResp.content
    def currentVersion = pageJson.version.number as int
    def currentBody    = pageJson.body.storage.value

    // 2. Build new row
    def statusColor = (ctx.status == 'SUCCESS') ? '#00875A' : '#DE350B'
    def errorsHtml  = ctx.errors.isEmpty()
        ? '<em style="color:grey;">none</em>'
        : ctx.errors.collect { "<li>${escapeHtml(it)}</li>" }.join('')
                    .with { "<ul>${it}</ul>" }

    def newRow = """
<tr>
  <td>${escapeHtml(ctx.startTime)}</td>
  <td><strong style="color:${statusColor};">${ctx.status}</strong></td>
  <td><a href="${escapeHtml(ctx.buildUrl)}">#${escapeHtml(ctx.buildNumber)}</a></td>
  <td>${escapeHtml(ctx.initiatedBy)}</td>
  <td>${errorsHtml}</td>
</tr>"""

    // 3. Inject row before closing </tbody>
    def updatedBody = currentBody.replace('</tbody>', "${newRow}\n</tbody>")
    if (!updatedBody.contains('</tbody>')) {
        // fallback: page structure unexpected – append row to table end
        updatedBody = currentBody + newRow
    }

    // 4. PUT updated page
    def payload = groovy.json.JsonOutput.toJson([
        version: [number: currentVersion + 1],
        title  : pageJson.title,
        type   : 'page',
        body   : [
            storage: [
                value         : updatedBody,
                representation: 'storage'
            ]
        ]
    ])

    httpRequest(
        url              : "${ctx.baseUrl}/rest/api/content/${pageId}",
        httpMode         : 'PUT',
        customHeaders    : [[name: 'Authorization', value: auth],
                            [name: 'Content-Type',  value: 'application/json']],
        requestBody      : payload,
        validResponseCodes: '200'
    )

    echo "Confluence log updated – page id=${pageId}, version=${currentVersion + 1}"
}

// ── helpers ───────────────────────────────────────────────────────
def buildAuthHeader(user, token) {
    def raw = "${user}:${token}".bytes.encodeBase64().toString()
    return "Basic ${raw}"
}

def resolveInitiator() {
    try {
        def causes = currentBuild.rawBuild.getCauses()
        if (!causes) return 'unknown'
        def cause = causes[0]
        // UserIdCause → real user
        if (cause.metaClass.respondsTo(cause, 'getUserName')) {
            return cause.getUserName() ?: cause.getUserId() ?: 'unknown'
        }
        // TimerTriggerCause, UpstreamCause, etc.
        return cause.getShortDescription() ?: 'unknown'
    } catch (e) {
        return 'unknown'
    }
}

def extractErrors(log) {
    def errorLines = []
    log.readLines().each { line ->
        if (line =~ /(?i)(error|exception|fatal|failed|failure)/) {
            def cleaned = line.trim()
            if (cleaned && !errorLines.contains(cleaned)) {
                errorLines << cleaned
            }
        }
    }
    return errorLines.take(15)   // cap at 15 distinct error lines
}

def escapeHtml(String s) {
    if (!s) return ''
    return s.replace('&', '&amp;')
            .replace('<', '&lt;')
            .replace('>', '&gt;')
            .replace('"', '&quot;')
}

def initialPageBody() {
    return """
<h2>Beta Pipeline Build Log</h2>
<p>Auto-generated by Jenkins. Each row represents one pipeline run.</p>
<table data-table-width="1200" data-layout="full-width">
  <colgroup>
    <col style="width:180px;" />
    <col style="width:100px;" />
    <col style="width:80px;"  />
    <col style="width:160px;" />
    <col style="width:680px;" />
  </colgroup>
  <thead>
    <tr>
      <th><strong>Initiated Date (UTC)</strong></th>
      <th><strong>Status</strong></th>
      <th><strong>Build #</strong></th>
      <th><strong>Initiated By</strong></th>
      <th><strong>Error Messages</strong></th>
    </tr>
  </thead>
  <tbody>
  </tbody>
</table>
"""
}
