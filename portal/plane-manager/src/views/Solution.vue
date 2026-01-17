<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { LxBadge, LxStateDisplay, LxForm, LxRow, LxStack, LxSection, LxDataGrid } from "@wntr/lx-ui";
import api from "../../api.js";

const route = useRoute();

const data = ref();

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

onMounted(() => {
  getSolution();
});
</script>
<template>
  <div>
    <div>{{ data?.solverStatus }}</div>
    <div>{{ data?.score }}</div>
    {{ hardScore }} ||| {{ softScore }}

    <div></div>

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

      <LxRow label="Rezultāts" columnSpan="4">
        <LxStack orientation="horizontal">
          <LxBadge
            :icon="
              hardScore === '-0' ? 'notification-success' : 'notification-error'
            "
            :value="`Hard rezultāts: ${hardScore}`"
            iconSet="material"
          />
          <LxBadge
            icon="notification-success"
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
          <LxRow columnSpan="2">
            <LxDataGrid />
          </LxRow>
        </LxSection>
      </template>
    </LxForm>
    <hr />
    <pre>{{ data }}</pre>
  </div>
</template>
