<script setup>
import { onMounted, ref, computed } from "vue";
import api from "../../api.js";
import { LxList, lxDateUtils, LxIcon, LxStack } from "@wntr/lx-ui";

const data = ref([]);

function getSolutions() {
  api()
    .get("/evrp")
    .then((response) => {
      data.value = response.data;
    });
}

const listDisplay = computed(() => {
  return data.value
    .slice()
    .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt))
    .map((item) => ({
      jobId: item.jobId,
      startedAt: lxDateUtils.formatFull(item.startedAt),
      href: { name: "solution", params: { id: item?.jobId } },
      name: item?.name,
    }));
});

onMounted(() => {
  getSolutions();
});
</script>
<template>
  <div>
    <LxList
      listType="1"
      :items="listDisplay"
      primaryAttribute="jobId"
      secondaryAttribute="startedAt"
      clickableAttribute="jobId"
    >
      <template #customItem="item">
        <LxStack
          orientation="horizontal"
          verticalAlignment="center"
          mode="grid"
          :horizontalConfig="['*', 'auto']"
        >
          <div>
            <p class="lx-primary">{{ item?.name }}</p>
            <p class="lx-secondary">{{ item?.jobId }}</p>
          </div>
          <div>
            <LxStack
              orientation="horizontal"
              verticalAlignment="center"
              kind="compact"
            >
              <LxIcon
                value="time"
                style="height: 1.25rem; fill: var(--color-label)"
              />
              <p class="lx-secondary">{{ item?.startedAt }}</p>
            </LxStack>
          </div>
        </LxStack>
      </template>
    </LxList>
  </div>
</template>
