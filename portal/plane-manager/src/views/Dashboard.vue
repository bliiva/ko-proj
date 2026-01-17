<script setup>
import api from '../../api.js';


function getSolutions() {
    api().get('/evrp').then((response) => {
        console.log(response.data);
    });
}

getSolutions();

</script>
<template>
    <div>
        1234
    </div>
</template>