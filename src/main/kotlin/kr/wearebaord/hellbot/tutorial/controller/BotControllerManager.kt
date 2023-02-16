package kr.wearebaord.hellbot.tutorial.controller

import kr.wearebaord.hellbot.tutorial.BotApplicationManager
import kr.wearebaord.hellbot.tutorial.BotGuildContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

class BotControllerManager {
    private val controllerFactories: MutableList<BotControllerFactory<*>>
    private val commands: MutableMap<String, Command>

    init {
        controllerFactories = ArrayList()
        commands = HashMap()
    }

    fun registerController(factory: BotControllerFactory<*>) {
        controllerFactories.add(factory)
        val controllerClass: Class<*>? = factory.controllerClass
        for (method in controllerClass!!.declaredMethods) {
            val annotation = method.getAnnotation(BotCommandHandler::class.java)
            annotation?.let { registerControllerMethod(controllerClass, method, it) }
        }
    }

    private fun registerControllerMethod(controllerClass: Class<*>?, method: Method, annotation: BotCommandHandler) {
        val commandName =
            annotation.name.ifEmpty { method.name.lowercase(Locale.getDefault()) }
        val usage: String? = annotation.usage.ifEmpty { null }
        val methodParameters = method.parameters
        if (methodParameters.isEmpty() || !methodParameters[0].type.isAssignableFrom(Message::class.java)) {
            return
        }
        method.isAccessible = true
        val parameters: MutableList<Class<*>> = ArrayList()
        for (i in 1 until methodParameters.size) {
            parameters.add(methodParameters[i].type)
        }
        val command = Command(commandName, usage, parameters, controllerClass, method)
        commands[command.name] = command
    }

    fun dispatchMessage(
        instances: MutableMap<Class<out BotController?>, BotController?>, prefix: String, message: Message,
        handler: BotCommandMappingHandler
    ) {
        val content = message.contentDisplay.trim { it <= ' ' }
        val separated = content.split("\\s+".toRegex(), limit = 2).toTypedArray()
        if (!separated[0].startsWith(prefix)) {
            return
        }
        val commandName = separated[0].substring(prefix.length)
        val command = commands[commandName]
        if (command == null) {
            handler.commandNotFound(message, commandName)
            return
        }
        val inputArguments: Array<String?> = if (separated.size == 1) arrayOfNulls(0) else separated[1].split(
            "\\s+".toRegex(),
            command.parameters.size.coerceAtLeast(0)
        ).toTypedArray()
        if (inputArguments.size != command.parameters.size) {
            handler.commandWrongParameterCount(
                message,
                command.name,
                command.usage,
                inputArguments.size,
                command.parameters.size
            )
            return
        }
        val arguments = arrayOfNulls<Any>(command.parameters.size + 1)
        arguments[0] = message
        for (i in command.parameters.indices) {
            val parameterClass = command.parameters[i]
            try {
                arguments[i + 1] = parseArgument(parameterClass, inputArguments[i])
            } catch (ignored: IllegalArgumentException) {
                handler.commandWrongParameterType(
                    message,
                    command.name,
                    command.usage,
                    i,
                    inputArguments[i],
                    parameterClass
                )
                return
            }
        }
        try {
            command.commandMethod.invoke(instances!![command.controllerClass], *arguments)
        } catch (e: InvocationTargetException) {
            handler.commandException(message, command.name, e.cause)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    private fun parseArgument(parameterClass: Class<*>, value: String?): Any? {
        return try {
            if (parameterClass == String::class.java) {
                value
            } else if (parameterClass == Int::class.javaPrimitiveType || parameterClass == Int::class.java) {
                Integer.valueOf(value)
            } else if (parameterClass == Long::class.javaPrimitiveType || parameterClass == Long::class.java) {
                java.lang.Long.valueOf(value)
            } else if (parameterClass == Boolean::class.javaPrimitiveType || parameterClass == Boolean::class.java) {
                parseBooleanArgument(value)
            } else if (parameterClass == Float::class.javaPrimitiveType || parameterClass == Float::class.java) {
                java.lang.Float.valueOf(value)
            } else if (parameterClass == Double::class.javaPrimitiveType || parameterClass == Double::class.java) {
                java.lang.Double.valueOf(value)
            } else {
                throw IllegalArgumentException()
            }
        } catch (ignored: NumberFormatException) {
            throw IllegalArgumentException()
        }
    }

    private fun parseBooleanArgument(value: String?): Boolean {
        return if ("yes" == value || "true" == value) {
            true
        } else if ("no" == value || "false" == value) {
            false
        } else {
            val integerValue = Integer.valueOf(value)
            if (integerValue == 1) {
                true
            } else if (integerValue == 0) {
                false
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    fun createControllers(
        applicationManager: BotApplicationManager?,
        context: BotGuildContext?,
        guild: Guild?
    ): List<BotController?> {
        val controllers: MutableList<BotController?> = ArrayList()
        for (factory in controllerFactories) {
            controllers.add(factory.create(applicationManager, context, guild))
        }
        return controllers
    }

    private class Command(
        val name: String,
        val usage: String?,
        val parameters: List<Class<*>>,
        val controllerClass: Class<*>?,
        val commandMethod: Method
    )
}