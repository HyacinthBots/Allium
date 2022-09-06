#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
import com.kotlindiscord.kord.extensions.extensions.Extension

#set($extName = $NAME.toLowerCase())
#parse("File Header.java")
class ${NAME}Extension : Extension() {
    override val name = "$extName"
    
    override suspend fun setup() {
        TODO("Not yet implemented")
    }
}
