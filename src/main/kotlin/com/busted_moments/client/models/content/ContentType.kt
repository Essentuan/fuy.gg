package com.busted_moments.client.models.content

import net.essentuan.esl.other.Printable
import java.util.UUID

interface ContentType : List<Stage.Builder>, Printable {
    val id: String
}