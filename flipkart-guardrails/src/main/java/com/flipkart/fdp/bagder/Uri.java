package com.flipkart.fdp.bagder;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Uri
{
    public static String getProcessData(Long processId)
    {
        return String.format("/view/%s", processId);
    }

    public static String getJobConfig(Long executionId, Long processId)
    {
        return String.format("/orchestrator/script/executions/%s/processes/%s", executionId, processId);
    }
}
