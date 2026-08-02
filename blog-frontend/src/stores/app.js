import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getSiteConfig } from '@/api/siteConfig'

export const useAppStore = defineStore('app', () => {
  const siteConfig = ref({})
  const loading = ref(false)

  const siteName = computed(() => siteConfig.value.site_name || siteConfig.value.siteName || 'My Blog')
  const siteDescription = computed(() => siteConfig.value.site_description || siteConfig.value.siteDescription || '')
  const aboutContent = computed(() => siteConfig.value.about || '')
  const avatar = computed(() => siteConfig.value.site_avatar || '')

  async function loadSiteConfig() {
    loading.value = true
    try {
      const res = await getSiteConfig()
      siteConfig.value = res.data || res
    } catch (e) {
      console.error('Failed to load site config:', e)
    } finally {
      loading.value = false
    }
  }

  return {
    siteConfig,
    loading,
    siteName,
    siteDescription,
    aboutContent,
    avatar,
    loadSiteConfig
  }
})
