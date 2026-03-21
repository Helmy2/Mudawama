package io.github.helmy2.mudawama.umbrella.ui

import io.github.helmy2.mudawama.core.data.di.iosCoreDataModule
import io.github.helmy2.mudawama.core.domain.session.Encryptor
import org.koin.core.context.startKoin

fun initializeKoin(iosEncryptor: Encryptor) {
    startKoin {
        modules(
            iosCoreDataModule(iosEncryptor = iosEncryptor)
        )
    }
}
