import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import type { GroupDetail } from '../types/chat'
import { resetGroupProfileDraftState } from '../utils/group-draft'

export const useGroupStore = defineStore('group', () => {
  const groupDetail = ref<GroupDetail | null>(null)
  const showGroupDrawer = ref(false)
  const noticeDraft = ref('')
  const groupProfileDraft = reactive({
    groupName: '',
    avatarPreview: '',
    avatarFile: null as File | null
  })

  function resetGroupDetailState() {
    groupDetail.value = null
    showGroupDrawer.value = false
    noticeDraft.value = ''
    resetGroupProfileDraftState(groupProfileDraft)
  }

  return {
    groupDetail,
    showGroupDrawer,
    noticeDraft,
    groupProfileDraft,
    resetGroupDetailState
  }
})
