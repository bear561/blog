<script setup>
// 设备皮肤开关：移动态且提供了 mobile 组件时渲染 mobile，否则桌面（兜底）。
// 组件经 markRaw 由调用方注入，避免被响应式代理包裹。
import { useDeviceMode } from '@/composables/useDeviceMode'

defineProps({
  desktop: { type: [Object, Function], required: true },
  mobile: { type: [Object, Function], default: null }
})

const { isMobile } = useDeviceMode()
</script>

<template>
  <component :is="(isMobile && mobile) ? mobile : desktop" />
</template>
