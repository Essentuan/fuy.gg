package com.busted_moments.client.framework.wynntils

import com.busted_moments.mixin.invoker.FunctionManagerInvoker
import com.google.common.primitives.Primitives
import com.wynntils.core.components.Managers
import com.wynntils.core.consumers.functions.arguments.FunctionArguments
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.annotatedWith
import net.essentuan.esl.reflections.extensions.classOf
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.javaClass
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.typeArgs
import net.essentuan.esl.reflections.extensions.typeInformationOf
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaType

private typealias WynntilsFunction<T> = com.wynntils.core.consumers.functions.Function<T>

private val DEFAULT_ARG = Any()

abstract class Function<T>(private vararg val aliases: String) : WynntilsFunction<T>() {
    private val func: KFunction<T>
    private val optional: Boolean?
    private val description: String

    init {
        func = this::class.declaredFunctions.first {
            it annotatedWith Call::class
        }.let {
            val ret = javaClass.typeInformationOf(Function::class.java)["T"]
            require(it.returnType.javaClass == ret) { "@Call must return ${ret?.simpleString() ?: "T"}!" }

            @Suppress("UNCHECKED_CAST")
            it as KFunction<T>
        }

        optional = if (!func.parameters.any { it.kind == KParameter.Kind.VALUE })
            null
        else
            func.parameters.any { it.kind == KParameter.Kind.VALUE && it.isOptional }

        description = func[Call::class]!!.value
    }

    final override fun getAliases(): List<String> {
        return aliases.toList()
    }

    override fun getArgumentDescription(argumentName: String): String =
        func.parameters.firstOrNull { it.name == argumentName }?.get(Description::class)?.value ?: "No Description"

    override fun getDescription(): String =
        description

    override fun getArgumentsBuilder(): FunctionArguments.Builder {
        if (optional == null)
            return FunctionArguments.RequiredArgumentBuilder.EMPTY

        val args = mutableListOf<FunctionArguments.Argument<*>>()
        for (arg in func.parameters) {
            if (arg.kind != KParameter.Kind.VALUE)
                continue

            val type = arg.type.javaType
            val cls = type.classOf()

            when (type) {
                List::class.java -> {
                    @Suppress("UNCHECKED_CAST")
                    args.add(
                        FunctionArguments.ListArgument(
                            arg.name!!,
                            type.typeArgs().getOrNull(0)?.classOf() as Class<Any>? ?: Any::class.java
                        )
                    )
                }

                else -> {
                    @Suppress("UNCHECKED_CAST")
                    args.add(
                        FunctionArguments.Argument<Any?>(
                            arg.name,
                            Primitives.wrap(cls as Class<Any?>),
                            if (arg.isOptional) DEFAULT_ARG else null
                        )
                    )
                }
            }
        }

        return if (optional)
            FunctionArguments.OptionalArgumentBuilder(args)
        else
            FunctionArguments.RequiredArgumentBuilder(args)
    }

    override fun getValue(args: FunctionArguments): T {
        val result = mutableMapOf<KParameter, Any?>()

        for (param in func.parameters) {
            if (param.kind == KParameter.Kind.INSTANCE)
                result[param] = this
            else {
                val value = args.getArgument<Any?>(param.name!!).value

                if (value != DEFAULT_ARG)
                    result[param] = value
            }
        }

        return func.callBy(result)
    }

    companion object {
        fun register() {
            Reflections.types
                .subtypesOf(WynntilsFunction::class)
                .map { it.instance }
                .filterNotNull()
                .forEach {
                    (Managers.Function as FunctionManagerInvoker).invokeRegisterFunction(it)
                }
        }
    }

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Call(val value: String = "No Description")

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Description(val value: String)
}