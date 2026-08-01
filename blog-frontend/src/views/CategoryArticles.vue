<script setup>
import { markRaw } from 'vue'
import DeviceView from '@/components/DeviceView.vue'
import Desktop from './desktop/CategoryArticlesDesktop.vue'
import Mobile from './mobile/CategoryMobile.vue'

const desktop = markRaw(Desktop)
const mobile = markRaw(Mobile)
</script>

<template>
  <DeviceView :desktop="desktop" :mobile="mobile" />
</template>
