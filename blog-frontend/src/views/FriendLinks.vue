<script setup>
import { markRaw } from 'vue'
import DeviceView from '@/components/DeviceView.vue'
import Desktop from './desktop/FriendLinksDesktop.vue'
import Mobile from './mobile/FriendLinksMobile.vue'

const desktop = markRaw(Desktop)
const mobile = markRaw(Mobile)
</script>

<template>
  <DeviceView :desktop="desktop" :mobile="mobile" />
</template>
