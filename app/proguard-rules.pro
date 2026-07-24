# The engine is pure Kotlin data/logic. Its persisted state goes through
# kotlinx.serialization, whose generated serializers must survive shrinking.
-keepclassmembers class eu.amiri.hokm.** {
    *** Companion;
}
-keepclasseswithmembers class eu.amiri.hokm.** {
    kotlinx.serialization.KSerializer serializer(...);
}
