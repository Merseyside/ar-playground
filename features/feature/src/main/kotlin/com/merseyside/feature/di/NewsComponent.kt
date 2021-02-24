package com.merseyside.feature.di

import com.merseyside.core.di.CoreComponent
import com.merseyside.core.di.scopes.FeatureScope
import dagger.Component

@FeatureScope
@Component(
    modules = [NewsModule::class],
    dependencies = [CoreComponent::class])
interface NewsComponent {
}
