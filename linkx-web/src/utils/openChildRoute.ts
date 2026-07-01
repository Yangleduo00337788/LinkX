import { openLinkxChildWindow, isElectronChildWindowApi } from './childWindow'

export async function openCreateGroupUi() {
  if (isElectronChildWindowApi()) {
    await openLinkxChildWindow({ type: 'create-group' })
    return
  }
  const url = `${window.location.origin}${window.location.pathname}#/child/create-group`
  window.open(url, 'linkx-create-group', 'width=718,height=427,menubar=no,toolbar=no')
}

export async function openAddFriendUi() {
  if (isElectronChildWindowApi()) {
    await openLinkxChildWindow({ type: 'add-friend' })
    return
  }
  const url = `${window.location.origin}${window.location.pathname}#/child/add-friend`
  window.open(url, 'linkx-add-friend', 'width=360,height=560,menubar=no,toolbar=no')
}

export async function openAddGroupMembersUi(groupId: string | number) {
  if (isElectronChildWindowApi()) {
    await openLinkxChildWindow({ type: 'add-group-members', groupId })
    return
  }
  const q = encodeURIComponent(String(groupId))
  const url = `${window.location.origin}${window.location.pathname}#/child/add-group-members?groupId=${q}`
  window.open(url, `linkx-add-members-${q}`, 'width=718,height=427,menubar=no,toolbar=no')
}