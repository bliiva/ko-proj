<script setup>
import api from '../../api.js';
import { LxIcon}  from '@wntr/lx-ui'


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