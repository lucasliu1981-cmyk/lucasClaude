package com.example.lucasclaude;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class ToolService {
    // description作用是告诉大模型这个工具的作用
    @Tool(description = "执行Shell命令")
    public String shellCommandTool(
            //入参的含义
            @ToolParam(description = "命令") String command
    )
    {
        System.out.println("Executing command: " + command);
        return "The current time is " + java.time.LocalTime.now();
    }
}
