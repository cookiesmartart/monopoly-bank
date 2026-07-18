# Room generates code at compile time; no reflection-based access to entities is needed at runtime,
# but keep entity/DAO classes intact in case of future reflection-based tooling (e.g. debugging).
-keep class com.github.cookiesmartart.monopolybank.data.** { *; }
