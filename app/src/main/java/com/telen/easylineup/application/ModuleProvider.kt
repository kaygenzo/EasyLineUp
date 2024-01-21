/*
    Copyright (c) Karim Yarboua. 2010-2024
*/

package com.telen.easylineup.application

import com.telen.easylineup.domain.DomainModule
import com.telen.easylineup.repository.RepositoryModule
import org.koin.core.module.Module

object ModuleProvider {
    val modules: List<Module>
        get() = ArrayList<Module>().apply {
            add(appModules)
            add(DomainModule.domainModules)
            add(RepositoryModule.repositoryModules)
        }
}
