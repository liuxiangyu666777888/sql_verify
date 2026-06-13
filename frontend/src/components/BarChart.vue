<template>
  <div>
    <div class="flex items-end gap-2 mb-4 border-b border-outline-variant pb-2" style="height:130px">
      <div
        v-for="bar in bars"
        :key="bar.label"
        class="flex flex-1 flex-col items-center justify-end group"
      >
        <div
          class="w-full rounded-t-sm transition-all group-hover:opacity-80"
          :class="bar.color"
          :style="{ height: bar.pct + '%' }"
        ></div>
        <span class="mt-2 text-xs font-semibold text-on-surface-variant">{{ bar.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
export interface BarItem {
  label: string
  pct: number
  color: string
}

defineProps<{
  bars: BarItem[]
}>()
</script>
