package com.flipkart.fdp.bagder;

public class Uri
{
    public Uri()
    {
    }

    public static String getProcessData(Long processId)
    {
        return String.format("/view/%s", processId);
    }
}
