<template lang="html">
<div class="menu-container">
  <expand></expand>
  <selection></selection>
  <insert-box v-if="viewMode === 'edit'"></insert-box>
  <move-box v-if="viewMode === 'edit'"></move-box>
  <edit-del v-if="viewMode === 'edit'"></edit-del>
  <sequence-box v-if="viewMode === 'edit'"></sequence-box>
  <progress-box v-if="viewMode === 'edit'"></progress-box>
</div>
</template>

<script>
import insertBox from './insertBox'
import moveBox from './moveBox'
import editDel from './editDel'
import sequenceBox from './sequenceBox'
import progressBox from './progressBox'
import expand from './expand'
import selection from './selection'
import { mapGetters } from 'vuex'

export default {
  name: 'editMenu',
  components: {
    insertBox,
    moveBox,
    editDel,
    sequenceBox,
    progressBox,
    expand,
    selection
  },
  computed: {
    ...mapGetters(['getViewMode']),
    viewMode() {
      return this.getViewMode
    }
  }
}
</script>
