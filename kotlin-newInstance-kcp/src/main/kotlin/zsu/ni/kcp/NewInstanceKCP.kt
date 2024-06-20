package zsu.ni.kcp

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.js.translate.intrinsic.functions.FunctionIntrinsics
import zsu.ni.kcp.backend.NewInstanceIrGenerationExtension

/**
 * 1. jvm intrinsics are able to modify in [IrGenerationExtension.getPlatformIntrinsicExtension], but only jvm currently.
 * 2. js intrinsics are defined in [FunctionIntrinsics] but not exposed as an extension.
 */
@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class NewInstanceKCP : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        NewInstanceIrGenerationExtension.register(this)
    }
}


