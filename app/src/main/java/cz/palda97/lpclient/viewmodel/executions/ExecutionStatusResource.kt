package cz.palda97.lpclient.viewmodel.executions

import cz.palda97.lpclient.R
import cz.palda97.lpclient.model.entities.execution.ExecutionStatus

val ExecutionStatus.resource: Int
    get() = when(this) {
        ExecutionStatus.FINISHED -> R.drawable.ic_baseline_done_24
        ExecutionStatus.FAILED -> R.drawable.ic_baseline_clear_24
        ExecutionStatus.RUNNING -> R.drawable.ic_baseline_directions_run_24
        ExecutionStatus.CANCELLED -> R.drawable.ic_baseline_done_24_yellow
        ExecutionStatus.DANGLING -> R.drawable.ic_baseline_settings_24
    }