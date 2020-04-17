package example.di

import com.vans.di.Qualifier

sealed class ConstantQualifier: Qualifier {
    object Endpoint: ConstantQualifier()
    object PageSize: ConstantQualifier()
}
