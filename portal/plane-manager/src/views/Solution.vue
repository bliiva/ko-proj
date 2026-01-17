<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import {
  LxBadge,
  LxStateDisplay,
  LxForm,
  LxRow,
  LxStack,
  LxSection,
  LxDataGrid,
  LxContentSwitcher,
  LxInfoWrapper,
} from "@wntr/lx-ui";
import api from "../../api.js";

const route = useRoute();

const data = ref();
const switcherModel = ref("full");

function getSolution() {
  api()
    .get(`/evrp/${route.params.id}`)
    .then((response) => {
      console.log(response);
      data.value = response.data;
    });
}

const hardScore = computed(() => {
  if (!data.value) return 0;
  const parts = data.value.score.split("/");
  // split in -28hard in number and text
  const number = parts[0].match(/-?\d+/);
  const text = parts[0].match(/[a-zA-Z]+/);
  return number[0];
});

const softScore = computed(() => {
  if (!data.value) return 0;
  const parts = data.value.score.split("/");
  const number = parts[1].match(/-?\d+/);
  const text = parts[1].match(/[a-zA-Z]+/);
  return number[0];
});

const visitData = ref();

function getTest() {
  api()
    .get(`/evrp/${route.params.id}/schedule`)
    .then((response) => {
      console.log("Test data:", response);
      visitData.value = response.data;
    });
}

const columnDefinitions = [
  {
    id: "id",
    name: "ID",
    attributeName: "id",
    size: "xs",
  },
  {
    id: "plane",
    name: "Lidmašīna",
    attributeName: "plane",
    size: "s",
    kind: "primary",
  },
  {
    id: "gate",
    name: "Vārti",
    attributeName: "gate",
    size: "s",
  },
  {
    id: "type",
    name: "Tips",
    attributeName: "typeDisplay",
    size: "s",
    type: "icon",
  },
  {
    id: "startTime",
    name: "Sākums",
    attributeName: "startTime",
    size: "s",
  },
  {
    id: "endTime",
    name: "Beigas",
    attributeName: "endTime",
    size: "s",
  },
];

const visitListDisplay = computed(() => {
  if (!data.value) return [];
  return data.value?.visitList
    ?.map((x) => ({
      ...x,
      typeDisplay: {
        label: x?.type === "ARRIVAL" ? "Ielidošana" : "Izlidošana",
        icon: x?.type === "ARRIVAL" ? "back" : "next",
      },
    }))
    .sort((a, b) => a.startTime - b.startTime);
});

function getGridItems() {
  if (switcherModel.value === "full") {
    return visitListDisplay.value || [];
  } else if (switcherModel.value === "arrivals") {
    return visitListDisplay.value?.filter((x) => x?.type === "ARRIVAL");
  } else if (switcherModel.value === "departure") {
    return visitListDisplay.value?.filter((x) => x?.type === "DEPARTURE");
  }
  return visitListDisplay.value;
}

function getColor(index){
  const colors = ['var(--color-green)', 'var(--color-blue)', 'var(--color-purple)', 'var(--color-orange)', 'var(--color-teal)'];
  return colors[index % colors.length];
}

onMounted(() => {
  getSolution();
  getTest();
});
</script>
<template>
  <div>
    <LxForm :columnCount="4">
      <template #header>
        {{ data?.name }}
      </template>
      <template #pre-header>
        <LxStateDisplay
          :value="data?.solverStatus"
          :dictionary="[
            {
              value: 'NOT_SOLVING',
              displayName: 'Pabeigts',
              displayType: 'finished',
            },
            {
              value: 'SOLVING_ACTIVE',
              displayName: 'Notiek risināšana',
              displayType: 'ongoing',
            },
          ]"
        />
      </template>
      <template #pre-header-info> Risinājuma statuss </template>

      <LxRow label="Rezultāts" columnSpan="4">
        <LxStack orientation="horizontal">
          <LxBadge
            :icon="hardScore === '-0' ? 'accept' : 'warning'"
            :value="`Hard rezultāts: ${hardScore}`"
            iconSet="material"
            :class="[
              {
                'lx-bage-error': hardScore !== '-0',
              },
            ]"
          />
          <LxBadge
            icon="accept"
            :value="`Soft rezultāts: ${softScore}`"
            iconSet="material"
          />
        </LxStack>
      </LxRow>
      <LxRow label="Termināļu skaits">
        <p class="lx-data">{{ data?.terminalList?.length }}</p>
      </LxRow>
      <LxRow label="Vārtu skaits">
        <p class="lx-data">{{ data?.gateList?.length }}</p>
      </LxRow>

      <LxRow label="Lidmašīnu skaits">
        <p class="lx-data">{{ data?.planeList?.length }}</p>
      </LxRow>
      <LxRow label="Aviokompāniju skaits">
        <p class="lx-data">{{ data?.companyList?.length }}</p>
      </LxRow>
      <template #sections>
        <LxSection label="Lidojumu saraksts" :columnCount="2">
          <LxRow columnSpan="2" :hideLabel="true">
            <LxContentSwitcher
              v-model="switcherModel"
              :items="[
                { id: 'full', name: 'Pilns saraksts' },
                { id: 'arrivals', name: 'Ielidošana' },
                { id: 'departure', name: 'Izlidošana' },
              ]"
            />
          </LxRow>
          <LxRow columnSpan="2" :hideLabel="true">
            <LxDataGrid
              :items="getGridItems()"
              :column-definitions="columnDefinitions"
              :scrollable="true"
            />
          </LxRow>
        </LxSection>
        <LxSection label="Vārtu grafiks" :columnCount="2">
          <LxRow columnSpan="2">
            <div style="overflow-x: scroll; padding-bottom: 0.5rem;">
            <div v-for="gate in visitData" style="display: flex; gap: 1rem; align-items: center;">
              <p>{{ gate.gateId }}</p>
              <div style="display: flex; gap: 0.125rem; padding-top: 0.5rem; padding-bottom: 0.5rem;">
                <div
                  v-for="(visit, index) in gate.visits"
                  :key="visit.id"
                  :style="{
                    width: `${(visit.endTime - visit.startTime) * 2}px`,
                    backgroundColor:
                      visit.type === 'ARRIVAL' ? '#90caf9' : '#a5d6a7',
                    border: '1px solid #000',
                    padding: '0.25rem',
                    paddingTop: '0.75rem',
                    paddingBottom: '0.75rem',
                    backgroundColor: getColor(index),
                  }"
                >
                  {{ visit.planeId }}
                </div>
              </div>
            </div>
            </div>
          </LxRow>
          <pre>{{ visitData }}</pre>
        </LxSection>
      </template>
    </LxForm>
    <pre>{{ data }}</pre>
  </div>
</template>
