package xyz.malefic.malefikeep.api

import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import xyz.malefic.malefikeep.db.DatabaseManager

@InitApi
fun initApi(ctx: InitApiContext) {
    DatabaseManager.init()
}
