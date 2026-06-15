# [OPEN] group-not-showing

## Problem
- Symptom: 创建群聊后，消息页面没有显示新群。
- Expected: 建群成功后，当前用户应能在会话列表看到新群。

## Scope
- Do not modify business logic before runtime evidence is collected.
- First focus on database evidence and actual persisted state.

## Hypotheses
1. `sys_friend` contains invalid `friend_id` values that no longer exist in `sys_user`.
2. Group creation writes group data, but does not create `im_session` rows for the current user.
3. Group creation writes `im_group_info`, but misses `im_group_member` rows for the current user.
4. The app is connected to a different database state than the one being inspected manually.

## Evidence Plan
- Verify datasource config used by the backend.
- Query `sys_user`, `sys_friend`, `im_group_info`, `im_group_member`, `im_session`.
- Compare friend IDs seen in debug logs with actual database rows.

## Status
- Session initialized.
- Database evidence collected.

## Evidence
- Backend datasource points to `linkx_im` with `root/root`.
- `sys_friend` for user `2065692265058017282` currently contains only valid friends:
  - `100000000000000101`
  - `100000000000000105`
- `im_group_info` row count is `0`.
- `im_group_member` rows for user `2065692265058017282` is `0`.
- `im_session` rows for user `2065692265058017282` with `session_type = 2` is `0`.
- Actual database schema differs from current code expectations:
  - `im_group_info` uses `group_no`, `avatar_url`, `status`
  - `im_group_member` uses `member_role`, `join_time`, `mute_until`
- Runtime inspection found no active listeners on ports `8080`, `5173`, or `7777`.
- Visible `java.exe` processes were IDE build/language-server related, not the Spring Boot backend.
- After restarting the backend, direct API verification succeeded:
  - login with `apifox_test / 123456` succeeded
  - `GET /api/friend/list` returned valid friend IDs `100000000000000101` and `100000000000000105`
  - `POST /api/group` with those IDs succeeded
  - `GET /api/chat/sessions` returned a new group session with `sessionType = 2`

## Hypothesis Results
- H1 `sys_friend` has invalid friend IDs: Rejected for current database state.
- H2 group creation does not write `im_session`: Supported by current data (`0` group sessions).
- H3 group creation does not write `im_group_member`: Supported by current data (`0` memberships).
- H4 inspected DB differs from app DB: Rejected for current datasource configuration.
- Additional runtime note: if the backend is stopped, any observed `用户不存在` message cannot be a new backend response from the current local environment.
- New result: current backend + current database can create groups successfully through the API. The remaining issue is now isolated to the user-facing client runtime/environment.

## Next Step
- Because both group tables are empty, prefer dropping old group tables and recreating them with the schema expected by the current Java entity model instead of doing fragile column-level migration.
