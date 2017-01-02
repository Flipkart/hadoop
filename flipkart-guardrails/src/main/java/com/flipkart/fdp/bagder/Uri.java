package com.flipkart.fdp.bagder;

public class Uri
{
    public Uri()
    {
    }

    public static String getProcessData(String hostPort, Long processId)
    {
        return String.format("http://%s/view/%s", hostPort, processId);
    }
}
