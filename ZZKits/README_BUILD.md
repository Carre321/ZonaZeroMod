# ZZKits (source)

## Compilar con Maven
Requisitos:
- Java 25
- Maven 3.9+

Comando:
```bash
mvn clean package
```

Salida:
- `target/ZZKits.jar` (shade plugin lo deja como `ZZKits.jar`)

## Estructura UI
Los documentos de CustomUI están en:
- `src/main/resources/Common/UI/Custom/`

Nota:
- En `ZZKitsMenu.ui` el import correcto es: `$Common = "Common.ui";`
- Los paneles `Utility/Tools/Backpack` están marcados como `Visible: false` para no romper el binding, pero no se muestran.
