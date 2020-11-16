include("poc:api", "poc:application", "poc:datasource", "poc:model", "poc:security", "poc:support")

buildCache {
    local {
        removeUnusedEntriesAfterDays = 1
    }
}