# ForgeFlow UI

You are building the production-quality React frontend for an existing backend called FlowForge.

IMPORTANT:
This is NOT a mockup and NOT a frontend-only demo.

The backend already exists and exposes REST APIs. The frontend must integrate with the real backend.

Build the frontend in React + TypeScript using:
- React
- TypeScript
- React Query / TanStack Query
- Axios
- React Router
- Tailwind CSS
- shadcn/ui
- Lucide icons

Do NOT invent backend APIs.
Do NOT use mock data.
Do NOT hardcode IDs.
Do NOT use fake delays.
Do NOT create fake success responses.

The frontend must be structured so API integration is centralized and easy to maintain.

==================================================
1. PRODUCT
==================================================

FlowForge is a workflow orchestration platform.

The user can:

1. Login
2. Maintain an authenticated session
3. View workspaces
4. Select a workspace
5. View workflows inside the selected workspace
6. Create workflows
7. Open a workflow builder
8. View workflow graph
9. Create/edit workflow nodes and edges
10. Save/publish workflow versions
11. Execute a workflow
12. View execution status
13. View execution details
14. View execution history
15. Logout

The UI should feel like a modern developer/productivity platform similar to:
- Temporal
- Airflow
- n8n
- Linear
- GitHub Actions

Clean, technical, professional UI.

==================================================
2. AUTHENTICATION
==================================================

Backend login endpoint:

POST
/api/v1/auth/login

Request:

{
  "email": "test@example.com",
  "password": "password123"
}

Actual response shape:

{
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "success": true,
  "timestamp": "..."
}

IMPORTANT:

The access token is inside:

response.data.accessToken

The refresh token is inside:

response.data.refreshToken

Do NOT expect:
response.user
response.accessToken

The login response does NOT contain a user object.

After successful login:

1. Store accessToken securely for the frontend architecture.
2. Store refreshToken.
3. Configure Axios to automatically attach:

Authorization: Bearer <accessToken>

to every protected API request.

4. Set authenticated state.
5. Navigate to /dashboard.

==================================================
3. REFRESH TOKEN LOGIC
==================================================

Implement automatic refresh-token handling.

This is REQUIRED.

Access tokens expire after approximately 900 seconds.

Axios must have:

REQUEST INTERCEPTOR

Before protected requests:
- attach current access token.

RESPONSE INTERCEPTOR

If a request returns:

401 Unauthorized

then:

1. Do NOT immediately logout.
2. Call:

POST
/api/v1/auth/refresh

Use the refresh token.

The exact refresh request/response contract must be isolated in authApi.ts so it can easily be adjusted if the backend contract differs.

Expected conceptual request:

{
  "refreshToken": "<refresh-token>"
}

Expected conceptual response:

{
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "success": true
}

When refresh succeeds:

1. Replace stored access token.
2. Replace refresh token if a new one is returned.
3. Retry the original failed request exactly once.

IMPORTANT:
Prevent infinite refresh loops.

Use an isRefreshing flag / refresh queue mechanism.

If multiple API calls receive 401 simultaneously:

- only ONE refresh request should be sent.
- other failed requests should wait for the refresh result.
- once refreshed, retry all queued requests with the new access token.

If refresh fails:

1. Clear access token.
2. Clear refresh token.
3. Clear authenticated state.
4. Clear React Query cache.
5. Navigate to /login.

==================================================
4. LOGOUT
==================================================

Provide a visible Logout action in the application sidebar/header.

Logout should:

1. Call backend logout API if available:

POST
/api/v1/auth/logout

If backend logout requires a token, attach Authorization.

2. Clear accessToken.
3. Clear refreshToken.
4. Clear authenticated user/session state.
5. Clear React Query cache.
6. Navigate to /login.

Even if the backend logout request fails, the frontend must still clear local authentication state.

==================================================
5. AUTHENTICATED ROUTES
==================================================

Public:

/login

Protected:

/dashboard

/workspaces/:workspaceId/workflows

/workspaces/:workspaceId/workflows/:workflowId

/workspaces/:workspaceId/workflows/:workflowId/history

/workspaces/:workspaceId/executions/:executionId

If an unauthenticated user accesses a protected route:

redirect to /login.

If an authenticated user accesses /login:

redirect to /dashboard.

Do not implement authentication using only component state.

Persist the session so refreshing the browser does not immediately log the user out while valid credentials/tokens exist.

==================================================
6. API CLIENT
==================================================

Create:

src/lib/apiClient.ts

Centralize Axios configuration here.

Responsibilities:

- baseURL
- Authorization header
- refresh token interceptor
- 401 handling
- retry failed requests
- logout on refresh failure
- consistent ApiError handling

Do NOT put Axios logic directly into components.

==================================================
7. API RESPONSE UNWRAPPING
==================================================

VERY IMPORTANT.

Backend responses are generally wrapped like:

{
  "data": ...,
  "success": true,
  "timestamp": "..."
}

Frontend hooks must unwrap response.data before returning data to components.

For example:

Backend:

{
  "data": [
    {...},
    {...}
  ],
  "success": true
}

React component must receive:

[
  {...},
  {...}
]

NOT:

{
  data: [...]
}

Do this consistently for every API.

This prevents errors such as:

workspaces.find is not a function

and:

workflows.filter is not a function

==================================================
8. WORKSPACES
==================================================

After successful login:

GET

/api/v1/workspaces

Expected conceptual response:

{
  "data": [
    {
      "id": "uuid",
      "name": "My Workspace",
      "description": "...",
      "createdAt": "...",
      "updatedAt": "..."
    }
  ],
  "success": true
}

WorkspaceContext should:

1. Fetch workspaces.
2. Unwrap data.
3. Store workspace array.
4. Automatically select the first workspace if none is selected.
5. Persist selectedWorkspaceId locally.
6. Restore selectedWorkspaceId after page refresh.
7. If persisted workspace no longer exists, select the first available workspace.

Workspace selector should appear in the application sidebar/header.

Changing workspace should:

1. Update selectedWorkspaceId.
2. Clear workflow-related React Query cache if necessary.
3. Fetch workflows for the newly selected workspace.
4. Navigate to that workspace's workflow/dashboard view.

==================================================
9. DASHBOARD
==================================================

Dashboard displays workflows for the selected workspace.

API:

GET

/api/v1/workspaces/{workspaceId}/workflows

The workspaceId MUST come from:

WorkspaceContext.selectedWorkspaceId

Never hardcode it.

The dashboard should show:

- Workflow name
- Description
- Status
- Version
- Updated time
- Open workflow button
- History button

Search should be client-side against loaded workflows.

==================================================
10. CREATE WORKFLOW
==================================================

Create workflow button opens modal.

Fields:

Name
Description

API:

POST

/api/v1/workspaces/{workspaceId}/workflows

Request body:

{
  "name": "My Workflow",
  "description": "Example workflow"
}

IMPORTANT:

workspaceId belongs in the URL.

Do NOT send workspaceId in the request body unless the backend explicitly requires it.

After successful creation:

1. Show success toast.
2. Invalidate workflows query.
3. Navigate to the newly created workflow builder.

==================================================
11. WORKFLOW BUILDER
==================================================

This is the most important screen.

Route:

/workspaces/:workspaceId/workflows/:workflowId

The builder must display:

LEFT SIDE:
Node palette.

Node types:

- HTTP
- AI_PROMPT
- CONDITION
- TRANSFORM
- DELAY
- etc. based on backend-supported node types.

CENTER:
Workflow graph canvas.

Use React Flow or equivalent.

RIGHT SIDE:
Selected node configuration panel.

Bottom/top toolbar:

- Save
- Publish
- Execute
- Version information

==================================================
12. LOAD WORKFLOW GRAPH
==================================================

When opening the workflow builder:

First obtain the correct workflow version.

DO NOT ever construct:

/versions/undefined/graph

Version ID must never be undefined.

Expected flow:

1. Load workflow metadata.
2. Determine current/latest version.
3. Store workflowVersionId.
4. Then request graph.

Graph API:

GET

/api/v1/workspaces/{workspaceId}/workflows/{workflowId}/versions/{workflowVersionId}/graph

Only make this request when workflowVersionId exists.

Do not render graph request until version ID is available.

If there is no version:

show appropriate empty state or create-version flow.

==================================================
13. GRAPH RESPONSE
==================================================

The graph response contains workflow nodes and edges.

Map backend graph objects into React Flow nodes and edges.

Do not assume backend field names blindly.

Create a dedicated mapper:

src/lib/mappers/workflowGraphMapper.ts

Responsibilities:

Backend graph → React Flow graph.

React Flow graph → backend graph request.

Do not mix backend DTOs with UI state types.

==================================================
14. NODE CONFIGURATION
==================================================

When a node is selected:

show configuration panel.

For each node type render appropriate fields.

AI_PROMPT example:

Prompt textarea.

Support expressions such as:

{{input.userId}}

HTTP node:

- URL
- HTTP method
- headers
- body

Condition node:

- condition/expression

Transform:

- transformation expression

Delay:

- duration

Do not invent backend-supported configuration fields.

Keep node configuration generic enough to adapt to backend DTOs.

==================================================
15. SAVE / VERSIONING
==================================================

Workflow versioning is important.

The UI must clearly display:

Current version:
v1
v2
v3

When saving/publishing:

Use the backend's existing workflow version APIs.

Do not create fake frontend-only versions.

After successful save/publish:

- invalidate workflow graph queries
- invalidate workflow metadata queries
- update displayed version
- show toast

==================================================
16. EXECUTE WORKFLOW
==================================================

Execute button opens execution dialog.

Allow JSON input.

Example:

{
  "userId": 1
}

The backend execution request contains workflow input.

Execution should use:

POST

/api/v1/workflows/versions/{workflowVersionId}/execute

OR the exact execution endpoint already exposed by the backend.

IMPORTANT:

Use the actual backend API contract supplied/configured in the API layer.

Do NOT hardcode a different URL inside the component.

The execution request must include the input JSON.

After successful execution:

Backend returns a queued execution result containing executionId.

Navigate to:

/workspaces/:workspaceId/executions/:executionId

==================================================
17. EXECUTION STATUS
==================================================

Execution states:

QUEUED
RUNNING
SUCCESS
FAILED

Execution page displays:

- execution ID
- workflow version
- status
- startedAt
- completedAt
- input
- output
- errorMessage

API:

GET

/api/v1/workflow-executions/{executionId}

Use the actual existing backend endpoint.

==================================================
18. EXECUTION POLLING
==================================================

Execution is asynchronous because backend uses:

Kafka
Transactional Outbox
Workflow Execution Consumer

Therefore the UI must NOT assume execution is immediately SUCCESS.

Execution flow:

QUEUED
↓
RUNNING
↓
SUCCESS / FAILED

While status is:

QUEUED or RUNNING

poll the execution endpoint.

Recommended:

every 2 seconds.

Stop polling when:

SUCCESS
FAILED

Do not poll forever.

Show:

"Queued..."
"Running..."
"Completed"
"Failed"

appropriately.

==================================================
19. EXECUTION HISTORY
==================================================

History page:

/workspaces/:workspaceId/workflows/:workflowId/history

Display executions for a workflow version.

Columns:

- Execution ID
- Version
- Status
- Started
- Completed
- Duration

Clicking an execution navigates to:

/workspaces/:workspaceId/executions/:executionId

Use real backend execution-history API.

Do not invent pagination parameters unless backend supports them.

==================================================
20. ERROR HANDLING
==================================================

Backend errors are wrapped like:

{
  "success": false,
  "timestamp": "...",
  "errors": [
    {
      "code": "RESOURCE_NOT_FOUND",
      "message": "Workflow not found"
    }
  ]
}

Create a centralized error parser.

UI should display:

errors[0].message

when available.

Never display:

"undefined"
"Request failed"
"Something went wrong"

when backend provides a useful message.

Handle:

400
401
403
404
409
429
500

appropriately.

401:
refresh token automatically.

403:
show permission error.

404:
show not found.

409:
show conflict message.

429:
show rate-limit message.

500:
show server error.

==================================================
21. RATE LIMITING
==================================================

Backend has workflow execution rate limiting.

If backend returns:

429 TOO_MANY_REQUESTS

show a clear message:

"Execution rate limit exceeded. Please try again later."

Do not automatically retry 429 requests.

==================================================
22. LOADING STATES
==================================================

Every API-driven screen must have loading states.

Examples:

Loading workspaces...

Loading workflows...

Loading workflow graph...

Loading execution...

Do not render components that assume data exists before loading completes.

==================================================
23. EMPTY STATES
==================================================

Workspace:

"No workspaces found"

Workflow:

"No workflows yet"

Graph:

"No workflow nodes yet"

Execution history:

"No executions yet"

==================================================
24. NAVIGATION RULES
==================================================

Sidebar:

Dashboard
Workflows
Current Workspace
Execution History
Logout

Workflow card:

Open → Workflow Builder

History → Execution History

Execute:

Execution page

Execution history row:

Execution detail

Breadcrumbs:

Workspace
→ Workflow
→ Version
→ Execution

Back buttons must preserve workspace/workflow context.

==================================================
25. REACT QUERY
==================================================

Use query keys carefully.

Examples:

['workspaces']

['workflows', workspaceId]

['workflow', workspaceId, workflowId]

['workflowGraph', workspaceId, workflowId, workflowVersionId]

['executions', workflowVersionId]

['execution', executionId]

Never use a single global key such as:

['workflows']

for all workspaces.

Invalidate the correct query after mutations.

==================================================
26. TYPESCRIPT TYPES
==================================================

Create strongly typed models:

AuthTokens
Workspace
Workflow
WorkflowVersion
WorkflowNode
WorkflowEdge
WorkflowGraph
WorkflowExecution
WorkflowExecutionStatus
ApiResponse<T>
ApiErrorResponse

Do not use:

any

unless absolutely unavoidable.

Backend DTOs and UI models should be separated where appropriate.

==================================================
27. API ORGANIZATION
==================================================

Create:

src/api/authApi.ts
src/api/workspaceApi.ts
src/api/workflowApi.ts
src/api/workflowVersionApi.ts
src/api/executionApi.ts

Hooks:

src/hooks/useAuth.ts
src/hooks/useWorkspaces.ts
src/hooks/useWorkflows.ts
src/hooks/useWorkflow.ts
src/hooks/useWorkflowGraph.ts
src/hooks/useExecutions.ts

Contexts:

src/context/AuthContext.tsx
src/context/WorkspaceContext.tsx

Utilities:

src/lib/apiClient.ts
src/lib/errorHandler.ts
src/lib/mappers/

Components should NEVER directly call axios.

==================================================
28. AUTH STORAGE
==================================================

Create one central token storage abstraction.

For example:

src/lib/authStorage.ts

Functions:

getAccessToken()
setAccessToken()
getRefreshToken()
setRefreshToken()
clearTokens()

Do not access localStorage directly throughout the application.

All token access must go through this abstraction.

==================================================
29. SECURITY
==================================================

Do not log:

accessToken
refreshToken
password

Do not put tokens in URLs.

Do not display tokens in UI.

Do not expose secrets.

Backend base URL must come from environment:

VITE_API_BASE_URL=http://localhost:8080

==================================================
30. API CONFIGURATION
==================================================

Create:

.env

VITE_API_BASE_URL=http://localhost:8080

Use:

import.meta.env.VITE_API_BASE_URL

Never hardcode localhost throughout API files.

==================================================
31. IMPORTANT BACKEND ARCHITECTURE
==================================================

The backend already implements:

Spring Boot
PostgreSQL
Redis
Kafka
Transactional Outbox
Idempotency
Retry
DLQ
Workflow execution
Rate limiting
Spring AI
Workflow versioning

The frontend must respect the asynchronous nature of the backend.

Do NOT make execution synchronous.

Do NOT fake execution completion.

Do NOT poll the graph endpoint unnecessarily.

==================================================
32. IDEMPOTENCY
==================================================

The backend supports execution idempotency.

If the execution API requires an idempotency key:

generate one UUID per intentional execution request.

IMPORTANT:

Generate it ONCE when the user clicks Execute.

If Axios retries the request because of token refresh:

reuse the SAME idempotency key.

Never generate a new idempotency key during an automatic retry.

This prevents duplicate workflow executions.

==================================================
33. UX FOR EXECUTION
==================================================

When Execute is clicked:

Disable Execute button.

Show:

"Submitting..."

After successful response:

"Execution queued"

Navigate to execution page.

Execution page:

QUEUED → "Waiting for worker..."
RUNNING → "Workflow is running..."
SUCCESS → "Execution completed"
FAILED → "Execution failed"

Show output JSON in a readable code viewer.

Show input JSON.

Show error message prominently on failure.

==================================================
34. REFRESH / BROWSER RELOAD
==================================================

Refreshing browser on:

/dashboard

must keep user logged in.

Refreshing:

/workspaces/:workspaceId/workflows/:workflowId

must restore:

- authentication
- workspace
- workflow
- workflow version
- graph

Refreshing execution page must restore execution state.

Do not depend on React state alone for route-critical information.

IDs must come from URL parameters.

==================================================
35. DO NOT MAKE THESE COMMON MISTAKES
==================================================

NEVER:

- use undefined workflowVersionId
- call .find() on an ApiResponse object
- call .filter() on an ApiResponse object
- assume login response contains user
- assume token is response.accessToken
- put workspaceId only in request body when backend requires URL
- hardcode workspace IDs
- hardcode workflow IDs
- hardcode version IDs
- call protected endpoints without Authorization
- logout immediately on the first 401
- create multiple refresh requests simultaneously
- retry failed requests infinitely
- retry 429 automatically
- poll completed executions
- create fake execution results
- use mock workspace/workflow data
- use fake API endpoints
- make graph requests with undefined version IDs

==================================================
36. DEVELOPMENT PRIORITY
==================================================

Implement in this order:

PHASE 1
Authentication
- Login
- token storage
- Axios interceptor
- refresh token
- logout
- protected routing

PHASE 2
Workspace
- workspace API
- WorkspaceContext
- workspace selector

PHASE 3
Workflow dashboard
- workflow list
- create workflow
- workspace-aware URLs

PHASE 4
Workflow builder
- load workflow
- load version
- load graph
- React Flow
- node configuration

PHASE 5
Versioning
- save
- publish
- version selection

PHASE 6
Execution
- execution input
- execute
- idempotency
- execution page
- polling

PHASE 7
History
- execution history
- execution detail

PHASE 8
Polish
- error handling
- loading states
- empty states
- toasts
- responsive UI
- logout
- refresh-session handling

==================================================
37. FINAL QUALITY REQUIREMENT
==================================================

Before considering the frontend complete, manually verify this exact end-to-end flow:

1. Open application.
2. User sees Login.
3. Login with valid credentials.
4. POST /api/v1/auth/login.
5. Access token and refresh token stored.
6. Navigate to Dashboard.
7. GET /api/v1/workspaces.
8. First workspace automatically selected.
9. GET /api/v1/workspaces/{workspaceId}/workflows.
10. Display workflows.
11. Click New Workflow.
12. POST /api/v1/workspaces/{workspaceId}/workflows.
13. Navigate to builder.
14. Load workflow metadata.
15. Obtain valid workflowVersionId.
16. GET graph using the real version ID.
17. Render graph.
18. Edit node.
19. Save/publish version.
20. Click Execute.
21. Generate ONE idempotency key.
22. POST execution request.
23. Receive executionId.
24. Navigate to execution page.
25. Show QUEUED.
26. Poll.
27. Show RUNNING.
28. Poll.
29. Show SUCCESS or FAILED.
30. Open history.
31. Open execution detail.
32. Wait for access token expiration / simulate 401.
33. Refresh token automatically.
34. Retry original request.
35. User remains logged in.
36. Click Logout.
37. Clear tokens.
38. Clear React Query cache.
39. Navigate to Login.

==================================================
38. IMPORTANT IMPLEMENTATION INSTRUCTION
==================================================

Do NOT simply generate all screens visually and assume APIs later.

Build the frontend around the real API/data flow.

For every screen, explicitly implement:

UI
→ React hook
→ API module
→ apiClient
→ backend endpoint
→ response unwrapping
→ typed result
→ React Query cache
→ UI state update
→ navigation

Before generating a component, determine:
- what data it needs
- which API provides it
- where IDs come from
- what happens while loading
- what happens on error
- what happens after success
- what route the user goes to next.

The application must be functional, not just visually convincing.

==================================================
39. DESIGN
==================================================

Use a polished dark/light developer-tool aesthetic.

Sidebar:
- FlowForge logo
- Workspace selector
- Dashboard
- Workflows
- Executions
- Logout

Dashboard:
- workflow cards
- search
- status badges
- version badges
- create workflow CTA

Builder:
- React Flow canvas
- node palette
- configuration inspector
- top toolbar
- version indicator
- execute button

Execution:
- status timeline
- metadata
- input/output JSON
- error details
- refresh/polling indicator

Use subtle animations but prioritize functionality.

Make responsive layouts.

==================================================
40. MOST IMPORTANT
==================================================

Do not stop after generating the UI.

Wire every screen to the backend.

If an API contract is uncertain, isolate it in the API module and clearly mark that single location for adjustment rather than scattering assumptions throughout the application.

The final application should be a real FlowForge client, not a prototype.

This project was built with [Lovable](https://lovable.dev).

## Build with Lovable

Continue developing this project in the [Lovable editor](https://lovable.dev/projects/53a85697-a064-41d8-8bd8-ce25fcdf933b).

- **Ship faster**: describe what you want to build and Lovable handles the code.
- **Stay in sync**: every change made in Lovable is committed straight to this repository.
- **Full ownership**: this code is yours. Push to `main` on GitHub and your changes sync back into Lovable, ready for your next prompt.

## Development

Prefer working locally? You need Node.js and npm — [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating).

```sh
git clone <this-repository-url>
cd <repository-name>
npm i
npm run dev
```
