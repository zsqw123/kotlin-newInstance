package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class NewInstanceIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        CacheableTransformer(moduleFragment, pluginContext).doTransform()
    }

    companion object {
        @OptIn(ExperimentalCompilerApi::class)
        fun register(storage: CompilerPluginRegistrar.ExtensionStorage) {
            with(storage) {
                IrGenerationExtension.registerExtension(NewInstanceIrGenerationExtension())
            }
        }
    }
}