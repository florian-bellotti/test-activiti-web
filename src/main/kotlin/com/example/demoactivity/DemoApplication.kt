package com.example.demoactivity


import org.activiti.api.process.model.ProcessDefinition
import org.activiti.api.process.model.ProcessInstance
import org.activiti.api.process.model.builders.ProcessPayloadBuilder
import org.activiti.api.process.runtime.ProcessRuntime
import org.activiti.api.process.runtime.connector.Connector
import org.activiti.api.runtime.shared.query.Pageable
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@RestController
class DemoApplication(private val processRuntime: ProcessRuntime) : CommandLineRunner {

    val processDefinition: List<ProcessDefinition>
        @GetMapping("/process-definitions")
        get() = processRuntime.processDefinitions(Pageable.of(0, 100)).getContent()


    @PostMapping("/documents")
    fun processFile(@RequestBody content: String): String {

        val processInstance = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("categorizeProcess")
                .withVariable(
                    "fileContent",
                    content
                )
                .build()
        )
        val message = ">>> Created Process Instance: $processInstance"
        println(message)
        return message
    }

    override fun run(vararg args: String) {}

    @Bean
    fun processTextConnector(): Connector {
        return Connector{ integrationContext ->
            val inBoundVariables = integrationContext.getInBoundVariables()
            val contentToProcess = inBoundVariables.get("fileContent") as String
            // Logic Here to decide if content is approved or not
            if (contentToProcess.contains("activiti")) {
                integrationContext.addOutBoundVariable(
                    "approved",
                    true
                )
            } else {
                integrationContext.addOutBoundVariable(
                    "approved",
                    false
                )
            }
            integrationContext
        }
    }

    @Bean
    fun tagTextConnector(): Connector {
        return Connector{ integrationContext ->
            var contentToTag = integrationContext.getInBoundVariables().get("fileContent") as String
            contentToTag += " :) "
            integrationContext.addOutBoundVariable(
                "fileContent",
                contentToTag
            )
            println("Final Content: $contentToTag")
            integrationContext
        }
    }

    @Bean
    fun discardTextConnector(): Connector {
        return Connector{ integrationContext ->
            var contentToDiscard = integrationContext.getInBoundVariables().get("fileContent") as String
            contentToDiscard += " :( "
            integrationContext.addOutBoundVariable(
                "fileContent",
                contentToDiscard
            )
            println("Final Content: $contentToDiscard")
            integrationContext
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(DemoApplication::class.java, *args)
        }
    }

}