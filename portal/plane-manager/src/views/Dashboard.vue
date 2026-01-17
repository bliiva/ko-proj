<script setup>
import { onMounted, ref, computed } from 'vue';
import api from '../../api.js';
import { LxList, lxDateUtils }  from '@wntr/lx-ui'

const data = ref([]);

function getSolutions() {
    api().get('/evrp').then((response) => {
        data.value = response.data;
    });
}

const listDisplay = computed(() => 
{
    return data.value
        .slice()
        .sort((a, b) => new Date(b.startedAt) - new Date(a.startedAt))
        .map((item) => ({
            jobId: item.jobId,
            startedAt: lxDateUtils.formatFull(item.startedAt),
            href: { name: 'solution', params: { id: item?.jobId } },
        }));
}
);

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
        />
    </div>
</template>