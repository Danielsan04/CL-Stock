
package com.example.clstock.presentacion.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FilaPdfMovimiento(
    val fecha: Long,
    val nombreProducto: String,
    val codigoBarra: String,
    val tipo: String,
    val cantidad: Int
)

object PdfReporteHelper {

    fun generarPdf(
        context: Context,
        uri: Uri,
        periodo: String,
        fechaDesde: Long,
        fechaHasta: Long,
        movimientos: List<FilaPdfMovimiento>
    ) {
        val documento = PdfDocument()

        // Tamaño A4 en puntos
        val ancho = 595
        val alto = 842

        // Colores del diseño
        val azulOscuro = Color.rgb(20, 45, 78)
        val azulPrincipal = Color.rgb(37, 99, 235)
        val verde = Color.rgb(21, 128, 61)
        val rojo = Color.rgb(220, 38, 38)
        val gris = Color.rgb(100, 116, 139)
        val linea = Color.rgb(226, 232, 240)

        val azulClaro = Color.rgb(239, 246, 255)
        val verdeClaro = Color.rgb(240, 253, 244)
        val rojoClaro = Color.rgb(254, 242, 242)

        val pintura = Paint(Paint.ANTI_ALIAS_FLAG)

        val formatoFecha = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

        val formatoFechaHora = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        val formatoHora = SimpleDateFormat(
            "dd/MM HH:mm",
            Locale.getDefault()
        )

        val fechaEmision = System.currentTimeMillis()

        val numeroReporte = SimpleDateFormat(
            "yyyyMMddHHmmss",
            Locale.getDefault()
        ).format(Date(fechaEmision))

        val entradas = movimientos
            .filter { it.tipo == "Entrada" }
            .sumOf { it.cantidad }

        val salidas = movimientos
            .filter { it.tipo == "Salida" }
            .sumOf { it.cantidad }

        val balance = entradas - salidas

        val cantidadMovimientos = movimientos.size

        var numeroPagina = 0
        var paginaActual: PdfDocument.Page? = null
        var posicionY = 0f

        fun texto(
            valor: String,
            x: Float,
            y: Float,
            tamano: Float = 10f,
            color: Int = Color.BLACK,
            negrita: Boolean = false,
            alineacion: Paint.Align = Paint.Align.LEFT
        ) {
            pintura.reset()
            pintura.isAntiAlias = true
            pintura.color = color
            pintura.textSize = tamano
            pintura.textAlign = alineacion
            pintura.typeface = if (negrita) {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else {
                Typeface.DEFAULT
            }

            paginaActual!!.canvas.drawText(
                valor,
                x,
                y,
                pintura
            )
        }

        fun rectangulo(
            izquierda: Float,
            arriba: Float,
            derecha: Float,
            abajo: Float,
            color: Int
        ) {
            pintura.reset()
            pintura.isAntiAlias = true
            pintura.color = color
            pintura.style = Paint.Style.FILL

            paginaActual!!.canvas.drawRect(
                izquierda,
                arriba,
                derecha,
                abajo,
                pintura
            )
        }

        fun lineaHorizontal(
            y: Float,
            color: Int = linea,
            grosor: Float = 1f
        ) {
            pintura.reset()
            pintura.isAntiAlias = true
            pintura.color = color
            pintura.strokeWidth = grosor

            paginaActual!!.canvas.drawLine(
                35f, y, 560f, y, pintura
            )
        }

        // Acorta textos demasiado largos para la tabla.
        fun ajustarTexto(
            valor: String,
            anchoMaximo: Float,
            tamano: Float
        ): String {
            val medidor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = tamano
                typeface = Typeface.DEFAULT
            }

            if (medidor.measureText(valor) <= anchoMaximo) {
                return valor
            }

            var resultado = valor

            while (
                resultado.isNotEmpty() &&
                medidor.measureText("$resultado...") > anchoMaximo
            ) {
                resultado = resultado.dropLast(1)
            }

            return "$resultado..."
        }

        fun iniciarPagina() {
            numeroPagina++

            val informacion = PdfDocument.PageInfo.Builder(
                ancho,
                alto,
                numeroPagina
            ).create()

            paginaActual = documento.startPage(informacion)

            // Encabezado
            texto(
                "CL STOCK",
                35f, 43f,
                23f, azulOscuro, true
            )

            texto(
                "SISTEMA DE CONTROL DE INVENTARIO",
                35f, 61f,
                8f, gris
            )

            texto(
                "REPORTE N.º $numeroReporte",
                560f, 35f,
                9f, azulOscuro, true,
                Paint.Align.RIGHT
            )

            texto(
                formatoFecha.format(Date(fechaEmision)),
                560f, 53f,
                10f, gris,
                alineacion = Paint.Align.RIGHT
            )

            lineaHorizontal(
                82f,
                azulPrincipal,
                2f
            )

            texto(
                "REPORTE DE MOVIMIENTOS DE ALMACEN",
                297f, 118f,
                15f, azulOscuro, true,
                Paint.Align.CENTER
            )

            posicionY = 150f
        }

        fun terminarPagina() {
            lineaHorizontal(790f)

            texto(
                "Generado desde CL Stock",
                35f, 808f,
                8f, gris
            )

            texto(
                "Pagina $numeroPagina",
                560f, 808f,
                8f, gris,
                alineacion = Paint.Align.RIGHT
            )

            documento.finishPage(paginaActual!!)
            paginaActual = null
        }

        fun encabezadoTabla() {
            texto(
                "Fecha y hora",
                40f, posicionY,
                9f, azulOscuro, true
            )

            texto(
                "Producto / Codigo",
                150f, posicionY,
                9f, azulOscuro, true
            )

            texto(
                "Tipo",
                440f, posicionY,
                9f, azulOscuro, true
            )

            texto(
                "Cant.",
                555f, posicionY,
                9f, azulOscuro, true,
                Paint.Align.RIGHT
            )

            posicionY += 12f
            lineaHorizontal(posicionY)
            posicionY += 15f
        }

        try {
            iniciarPagina()

            // Información general
            texto(
                "Periodo: $periodo",
                35f, posicionY,
                10f, azulOscuro
            )
            posicionY += 18f

            texto(
                "Desde: ${formatoFecha.format(Date(fechaDesde))}    " +
                        "Hasta: ${formatoFecha.format(Date(fechaHasta))}",
                35f, posicionY,
                10f, gris
            )
            posicionY += 18f

            texto(
                "Almacen: Almacen General",
                35f, posicionY,
                10f, azulOscuro
            )
            posicionY += 18f

            texto(
                "Fecha de emision: ${
                    formatoFechaHora.format(Date(fechaEmision))
                }",
                35f, posicionY,
                10f, gris
            )
            posicionY += 24f

            lineaHorizontal(posicionY)
            posicionY += 29f

            texto(
                "Resumen general",
                35f, posicionY,
                14f, azulOscuro, true
            )

            posicionY += 18f

            // Recuadros de resumen
            val arriba = posicionY
            val abajo = arriba + 72f

            rectangulo(
                35f, arriba,
                202f, abajo,
                azulClaro
            )

            rectangulo(
                214f, arriba,
                381f, abajo,
                verdeClaro
            )

            rectangulo(
                393f, arriba,
                560f, abajo,
                rojoClaro
            )

            texto(
                "Movimientos",
                118f, arriba + 21f,
                10f, gris,
                alineacion = Paint.Align.CENTER
            )

            texto(
                "$cantidadMovimientos",
                118f, arriba + 51f,
                23f, azulOscuro, true,
                Paint.Align.CENTER
            )

            texto(
                "Entradas",
                297f, arriba + 21f,
                10f, verde,
                alineacion = Paint.Align.CENTER
            )

            texto(
                "$entradas",
                297f, arriba + 51f,
                23f, verde, true,
                Paint.Align.CENTER
            )

            texto(
                "Salidas",
                476f, arriba + 21f,
                10f, rojo,
                alineacion = Paint.Align.CENTER
            )

            texto(
                "$salidas",
                476f, arriba + 51f,
                23f, rojo, true,
                Paint.Align.CENTER
            )

            posicionY = abajo + 30f

            texto(
                "Balance del periodo",
                35f, posicionY,
                12f, azulOscuro, true
            )

            texto(
                "${if (balance >= 0) "+" else ""}$balance unidades",
                560f, posicionY,
                14f, azulPrincipal, true,
                Paint.Align.RIGHT
            )

            posicionY += 22f
            lineaHorizontal(posicionY)
            posicionY += 30f

            texto(
                "Detalle de movimientos",
                35f, posicionY,
                14f, azulOscuro, true
            )

            posicionY += 28f

            encabezadoTabla()

            // Detalle de movimientos
            val movimientosOrdenados = movimientos.sortedByDescending {
                it.fecha
            }

            if (movimientosOrdenados.isEmpty()) {
                texto(
                    "No hay movimientos en el periodo seleccionado.",
                    40f, posicionY + 10f,
                    10f, gris
                )
            }

            movimientosOrdenados.forEachIndexed { indice, movimiento ->

                if (posicionY + 47f > 775f) {
                    terminarPagina()
                    iniciarPagina()
                    encabezadoTabla()
                }

                val filaArriba = posicionY - 12f
                val filaAbajo = posicionY + 29f

                if (indice % 2 == 0) {
                    rectangulo(
                        35f,
                        filaArriba,
                        560f,
                        filaAbajo,
                        Color.rgb(248, 250, 252)
                    )
                }

                val esEntrada = movimiento.tipo == "Entrada"
                val colorTipo = if (esEntrada) verde else rojo

                texto(
                    formatoHora.format(Date(movimiento.fecha)),
                    40f, posicionY + 3f,
                    9f, gris
                )

                texto(
                    ajustarTexto(
                        movimiento.nombreProducto,
                        260f,
                        9f
                    ),
                    150f, posicionY + 3f,
                    9f, azulOscuro, true
                )

                texto(
                    "Cod: ${
                        ajustarTexto(
                            movimiento.codigoBarra,
                            250f,
                            8f
                        )
                    }",
                    150f, posicionY + 18f,
                    8f, gris
                )

                texto(
                    movimiento.tipo,
                    440f, posicionY + 3f,
                    9f, colorTipo
                )

                texto(
                    "${if (esEntrada) "+" else "-"}${movimiento.cantidad}",
                    555f, posicionY + 3f,
                    9f, colorTipo, true,
                    Paint.Align.RIGHT
                )

                posicionY += 43f
                lineaHorizontal(posicionY - 5f)
            }

            terminarPagina()

            // Guardar el archivo seleccionado en Android
            val salida = context.contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException(
                    "No se pudo abrir el archivo PDF"
                )

            salida.use { documento.writeTo(it) }

        } finally {
            documento.close()
        }
    }
}
