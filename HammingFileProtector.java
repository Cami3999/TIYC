//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//Jaraja
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class HammingFileProtector {

    // Menú principal
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        byte[] originalData = null;
        byte[] protectedData = null;
        byte[] corruptedData = null;
        String currentFilename = "";
        int currentScheme = 0; // 1=8 bits, 2=256 bits, 3=4096 bits (*)
        StringBuilder hexContent = new StringBuilder();

        while (true) { // Cambio en el formato del menu (*)
            System.out.println("\n* MENÚ DE PROTECCIÓN DE ARCHIVOS CON HAMMING *");
            System.out.println("1. Cargar archivo (.txt)");
            System.out.println("2. Seleccionar esquema de Hamming");
            System.out.println("   1) 8 bits    -> .HA1");
            System.out.println("   2) 256 bits  -> .HA2");
            System.out.println("   3) 4096 bits -> .HA3");
            System.out.println("3. Codificar archivo");
            System.out.println("4. Introducir errores (máx. 1 por módulo)");
            System.out.println("5. Decodificar con corrección");
            System.out.println("6. Decodificar SIN corrección");
            System.out.println("7. Salir");
            System.out.print("Selecciona una opción: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Limpiar buffer

            try {
                switch (option) {
                    case 1:
                        System.out.print("Ingrese el nombre del archivo: ");
                        currentFilename = scanner.nextLine();
                        originalData = Files.readAllBytes(Paths.get(currentFilename));
                        System.out.println("Archivo cargado correctamente. Tamaño: " + originalData.length + " bytes");
                        break;

                    case 2:
                        // Seleccionar esquema
                        System.out.print("Esquema (1/2/3): ");
                        String sch = scanner.nextLine();
                        if ("1".equals(sch) || "2".equals(sch) || "3".equals(sch)) {
                            currentScheme = Integer.parseInt(sch);
                            System.out.println("Esquema " + currentScheme + " seleccionado");
                        } else {
                            System.out.println("Esquema no válido");
                        }
                        break;
                    case 3:
                        // Codificar
                        if (originalData == null) {
                            System.out.println("Primero carga un archivo");
                            break;
                        }
                        switch (currentScheme) {
                            case 1:
                                protectedData = hamming8Encode(originalData);
                                hexContent = Traduccion(protectedData);

                                // Guardar el string hexadecimal en el archivo
                                Files.write(Paths.get("Protected.HA1"), hexContent.toString().getBytes());
                                System.out.println("Archivo protegido con Hamming-8 guardado como " + "Protected.HA1 (formato hexadecimal)");
                                break;
                            case 2:
                                protectedData = hamming256Encode(originalData);
                                hexContent = Traduccion(protectedData);

                                // Guardar el string hexadecimal en el archivo
                                Files.write(Paths.get("Protected.HA2"), hexContent.toString().getBytes());
                                System.out.println("Archivo protegido con Hamming-256 guardado como " + "Protected.HA2 (formato hexadecimal)");
                                break;
                            case 3:

                                protectedData = hamming4096Encode(originalData);
                                hexContent = Traduccion(protectedData);

                                // Guardar el string hexadecimal en el archivo
                                Files.write(Paths.get("Protected.HA3"), hexContent.toString().getBytes());
                                System.out.println("Archivo protegido con Hamming-4096 guardado como " + "Protected.HA3 (formato hexadecimal)");
                                break;
                            default:
                                System.out.println("Selecciona un esquema primero");
                        }
                        break;
                    case 4:
                        // Introducir errores
                        if (protectedData == null) {
                            System.out.println("Primero codifica un archivo");
                            break;
                        }
                        int blockSize = (currentScheme == 1) ? 8 : (currentScheme == 2) ? 256 : 4096;
                        corruptedData = introduceErrors(protectedData, blockSize);
                        hexContent = Traduccion(corruptedData);

                            // Guardar el string hexadecimal en el archivo
                        switch (currentScheme) {
                            case 1: Files.write(Paths.get("Corrupted.HE1"), hexContent.toString().getBytes());
                                break;
                            case 2: Files.write(Paths.get("Corrupted.HE2"), hexContent.toString().getBytes());
                                break;
                            case 3: Files.write(Paths.get("Corrupted.HE3"), hexContent.toString().getBytes());
                                break;
                            default:
                                break;
                        }
                        System.out.println("Errores introducidos");
                        break;
                    case 5:
                        // Decodificar con corrección
                        if (corruptedData == null) {
                            System.out.println("Primero introduce errores");
                            break;
                        }
                        byte[] recoveredData;
                        switch (currentScheme) {
                            case 1:
                                recoveredData = hamming8Decode(corruptedData);
                                break;
                            case 2:
                                recoveredData = hamming256Decode(corruptedData);
                                break;
                            case 3:
                                recoveredData = hamming4096Decode(corruptedData);
                                break;
                            default:
                                System.out.println("Esquema no válido");
                                continue;
                        }
                        recoveredData = Arrays.copyOf(recoveredData, originalData.length);
                        switch (currentScheme) {
                            case 1: Files.write(Paths.get("Recovered.DC1"), recoveredData);
                                break;
                            case 2: Files.write(Paths.get("Recovered.DC2"), recoveredData);
                                break;
                            case 3: Files.write(Paths.get("Recovered.DC3"), recoveredData);
                                break;
                            default:
                                break;
                        }
                        System.out.println("Decodificado y corregido (Recovered.DCx)");
                        System.out.println("--- Original vs Recuperado ---");
                        System.out.println(new String(originalData, 0, Math.min(50, originalData.length)) + "...");
                        System.out.println(new String(recoveredData, 0, Math.min(50, recoveredData.length)) + "...");
                        break;
                    case 6:
                        // Decodificar sin corrección
                        if (corruptedData == null) {
                            System.out.println("Primero introduce errores");
                            break;
                        }
                        byte[] rawData;
                        switch (currentScheme) {
                            case 1:
                                rawData = hamming8DecodeNoCorrect(corruptedData);
                                //rawData = Arrays.copyOf(rawData, originalData.length);
                                Files.write(Paths.get("No_Corregido.DE1"), rawData);
                                break;
                            case 2:
                                rawData = hamming256DecodeNoCorrect(corruptedData);
                                //rawData = Arrays.copyOf(rawData, originalData.length);
                                Files.write(Paths.get("No_Corregido.DE2"), rawData);
                                break;
                            case 3:
                                rawData = hamming4096DecodeNoCorrect(corruptedData);
                                //rawData = Arrays.copyOf(rawData, originalData.length);
                                Files.write(Paths.get("No_Corregido.DE3"), rawData);
                                break;
                            default:
                                System.out.println("Esquema no válido");
                                continue;
                        }
                        System.out.println("=== TEXTO CON ERRORES ===");
                        System.out.println(new String(rawData));
                        break;
                    case 7:
                        System.out.println("Saliendo...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opción no válida");
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static StringBuilder Traduccion(byte[] Data){
        StringBuilder hexContent = new StringBuilder();
        for (byte b : Data) {    
            hexContent.append(String.format("%02X ", b)); // %02X = 2 dígitos en mayúsculas
        }
        return hexContent;
    }

    // Codificación Hamming para bloques de 8 bits (4 bits de datos + 4 bits de paridad)
    public static byte[] hamming8Encode(byte[] data) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte encoded;

        for (byte b : data) {
            // Partir en dos partes de 4
            for (int i = 0; i < 2; i++) {
                byte nupla = (byte) ((i == 0) ? ((b >> 4) & 0x0F) : (b & 0x0F));

                //Codificacion de bits

                //Bit de control c1 pos 7
                encoded = (byte) (((((nupla >> 3) & 1) ^ ((nupla >> 2) & 1) ^ ((nupla) & 1)) << 7) | 
                
                //bit de control c2 pos 6
                ((((nupla >> 3) & 1) ^ ((nupla >> 1) & 1) ^ ((nupla) & 1)) << 6) | 

                //Bit de dato d3, pos 5
                (((nupla >> 3) & 1) << 5) | 

                //Bit de control c4 pos 4
                ((((nupla >> 2) & 1) ^ ((nupla >> 1) & 1) ^ ((nupla) & 1)) << 4) |

                //Bit de dato d5 pos 3
                (((nupla >> 2) & 1) << 3) |
                
                //Bit de dato d6 pos 2
                (((nupla >> 1) & 1) << 2) |

                //Bit de dato d7 pos 1
                (((nupla) & 1) << 1) |
                //Bit de paridad 8            c1                               c2                                                        c4
                (((nupla >> 3) & 1) ^ ((nupla >> 2) & 1) ^ ((nupla) & 1)) ^ (((nupla >> 3) & 1) ^ ((nupla >> 1) & 1) ^ ((nupla) & 1)) ^ (((nupla >> 2) & 1) ^ ((nupla >> 1) & 1) ^ ((nupla) & 1)) ^ //Bits de control
                ((nupla >> 3) & 1) ^ ((nupla >> 2) & 1) ^ ((nupla >> 1) & 1) ^ ((nupla) & 1)  // bits de datos
                );

                output.write(encoded);
            }
        }

        return output.toByteArray();
    }

    // ------------------------------
    // Decodificación Hamming‑8 corregida
    public static byte[] hamming8Decode(byte[] encodedData) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean highNibble = true;
        int tempByte = 0;
    
        for (byte b : encodedData) {
            int bits = b & 0xFF; // Convertir a int para evitar problemas de signo
    
            // Calcular síndromes
            int s1 = ((bits >> 7) & 1) ^ ((bits >> 5) & 1) ^ ((bits >> 3) & 1) ^ ((bits >> 1) & 1);
            int s2 = ((bits >> 6) & 1) ^ ((bits >> 5) & 1) ^ ((bits >> 2) & 1) ^ ((bits >> 1) & 1);
            int s4 = ((bits >> 4) & 1) ^ ((bits >> 3) & 1) ^ ((bits >> 2) & 1) ^ ((bits >> 1) & 1);
            int errorPos = s1 + (s2 << 1) + (s4 << 2);
    
            // Calcular paridad global (XOR de todos los bits)
            int paridad = 0;
            for (int i = 0; i < 8; i++) {
                paridad ^= (bits >> i) & 1;
            }
    
            // Lógica de corrección
            if (errorPos != 0) {
                if (paridad == 1) {
                    // Error corregible (1 bit)
                    bits ^= (1 << (8 - errorPos));
                } else {
                    // Dos errores detectados (no corregible)
                    System.out.println("Dos errores detectados, no se puede corregir.");
                }
            } else if (paridad == 1) {
                // Error en el bit de paridad global
                bits ^= 1; // Corregir el bit de paridad
            }
    
            // Extraer nibble (d3, d5, d6, d7)
            int nibble = ((bits >> 5) & 1) << 3 | // d3
                         ((bits >> 3) & 1) << 2 | // d5
                         ((bits >> 2) & 1) << 1 | // d6
                         ((bits >> 1) & 1);       // d7
    
            // Combinar nibbles
            if (highNibble) {
                tempByte = nibble << 4;
                highNibble = false;
            } else {
                output.write(tempByte | nibble);
                highNibble = true;
            }
        }
    
        return output.toByteArray();
    }
    
    public static byte[] hamming8DecodeNoCorrect(byte[] encodedData) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean highNibble = true;
        int tempByte = 0;
    
        for (byte b : encodedData) {
            // Armar directamente el nibble
            int nibble = ((b >> 5) & 1) << 3 | // d3
                         ((b >> 3) & 1) << 2 | // d5
                         ((b >> 2) & 1) << 1 | // d6
                         ((b >> 1) & 1);       // d7
    
            if (highNibble) {
                tempByte = nibble << 4;
                highNibble = false;
            } else {
                tempByte |= nibble;
                output.write(tempByte);
                highNibble = true;
            }
        }
    
        return output.toByteArray();
    }
    

    // Codificación Hamming para bloques de 256 bits (simplificado)
    public static byte[] hamming256Encode(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int totalBits  = 256;
        final int r          = 9;                       // r bits de paridad (8 control + 1 global)
        final int dataBits   = totalBits - r;           // 247 bits de datos
        final int blockBytes = 30;       // 30 bytes de datos, 1 byte de bits de control y 1 byte de bit de paridad(se desperdician 7 bits).

        for (int i = 0; i < data.length; i += blockBytes) {
            int end   = Math.min(i + blockBytes, data.length);
            byte[] block = Arrays.copyOfRange(data, i, end);

            if (block.length < blockBytes) {
                byte[] padded = new byte[blockBytes];
                System.arraycopy(block, 0, padded, 0, block.length);
                block = padded;
            }

            // Array de bits para el código completo
            boolean[] code = new boolean[totalBits];

            //  Copiar bits de datos en posiciones que NO son potencias de 2
            int dataBitIndex = 0;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if ((bitPos & (bitPos - 1)) != 0) { // no es potencia de 2
                    int byteIdx = dataBitIndex / 8;
                    int bitIdx  = 7 - (dataBitIndex % 8);
                    boolean bit = false;
                    if (byteIdx < block.length) {
                        bit = ((block[byteIdx] >> bitIdx) & 1) == 1;
                    }
                    code[bitPos - 1] = bit;
                    dataBitIndex++;
                }
            }                                    

            //  Calcular bits de control(posiciones 1,2,4,8,16,32,64,128)
            for (int p = 0; p < r; p++) {
                int pos = 1 << p;
                boolean parity = false;   //Flaso = 0, True = Verdad
                for (int bitPos = pos; bitPos <= totalBits; bitPos++) {
                    if (bitPos != pos && ((bitPos & pos) != 0)) {
                        parity ^= code[bitPos - 1];
                    }
                }
                code[pos - 1] = parity;
            }

            // Calcular paridad global en posición 256 (2^(r-1))
            int globalPos = 1 << (r-1);
            boolean paridad = false;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if (bitPos != globalPos) {
                    paridad ^= code[bitPos - 1];
                }
            }
            code[globalPos - 1] = paridad;

            // 4) Empaquetar 8 bits en cada byte (big‑endian interno)
            for (int j = 0; j < totalBits; j += 8) {
                int val = 0;
                for (int k = 0; k < 8; k++) {
                    val = (val << 1) | (code[j + k] ? 1 : 0);
                }
                output.write(val);
            }
        }

        return output.toByteArray();
    }

    // Decodificación Hamming‑256 con detección (pero sin corrección real)
    public static byte[] hamming256Decode(byte[] encodedData) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int totalBits  = 256;
        final int r          = 9;               // 8 bits de paridad + 1 global
        final int codeBytes  = totalBits / 8;   // 32 bytes por bloque
        final int dataBits   = totalBits - r;   // 247 bits de datos
        final int blockBytes = dataBits / 8;    // 30 bytes de datos

        for (int i = 0; i < encodedData.length; i += codeBytes) {
            // 1) Leer bloque de 32 bytes como array de bits
            boolean[] code = new boolean[totalBits];
            for (int j = 0; j < codeBytes; j++) {
                int b = encodedData[i + j] & 0xFF;
                for (int k = 0; k < 8; k++) {
                    // el bit más significativo va primero
                    code[j * 8 + k] = ((b >> (7 - k)) & 1) == 1;
                }
            }

            // 2) Calcular síndrome (posición del error)
            int syndrome = 0;
            for (int p = 0; p < r - 1; p++) {
                int mask = 1 << p;
                boolean parity = false;
                for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                    if ((bitPos & mask) != 0) parity ^= code[bitPos - 1];
                }
                if (parity) syndrome |= mask;
            }
            // 3) Paridad global
            boolean paridad = false;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                paridad ^= code[bitPos - 1];
            }

            // 4) Corregir si hay un solo bit malo
            if (syndrome != 0) {
                if (paridad) {
                    // corrige el bit en posición 'syndrome'
                    int idx = syndrome - 1;
                    code[idx] = !code[idx];
                } else {
                    System.out.println("⚠️ Dos errores detectados en bloque " + (i / codeBytes));
                }
            }

            // 5) Extraer los bits de datos (posiciones que NO son potencias de 2)
            ByteArrayOutputStream blockOut = new ByteArrayOutputStream();
            int bitCount = 0;
            int currByte = 0;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if ((bitPos & (bitPos - 1)) != 0) {
                    currByte = (currByte << 1) | (code[bitPos - 1] ? 1 : 0);
                    bitCount++;
                    if (bitCount % 8 == 0) {
                        blockOut.write(currByte);
                        currByte = 0;
                    }
                }
            }
            // 6) Añadir los datos recuperados
            output.write(blockOut.toByteArray());
        }

        return output.toByteArray();
    }

    // Decodificación Hamming‑256 SIN corrección: simplemente saca los bytes de datos
    public static byte[] hamming256DecodeNoCorrect(byte[] encodedData) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int totalBits  = 256;
        final int r          = 9;               // 8 paridad primarios + 1 global
        final int codeBytes  = totalBits / 8;   // 32 bytes por bloque
        final int dataBits   = totalBits - r;   // 247 bits de datos
        final int blockBytes = dataBits / 8;    // 30 bytes de datos

        for (int i = 0; i < encodedData.length; i += codeBytes) {
            // 1) Leer bloque de 32 bytes a array de bits
            boolean[] code = new boolean[totalBits];
            for (int j = 0; j < codeBytes; j++) {
                int b = encodedData[i + j] & 0xFF;
                for (int k = 0; k < 8; k++) {
                    code[j * 8 + k] = ((b >> (7 - k)) & 1) == 1;
                }
            }
            // 2) Extraer los bits de datos (sin corregir errores)
            ByteArrayOutputStream blockOut = new ByteArrayOutputStream();
            int bitCount = 0, currByte = 0;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if ((bitPos & (bitPos - 1)) != 0) { // posición no potencia de 2
                    currByte = (currByte << 1) | (code[bitPos - 1] ? 1 : 0);
                    bitCount++;
                    if (bitCount % 8 == 0) {
                        blockOut.write(currByte);
                        currByte = 0;
                    }
                }
            }
            output.write(blockOut.toByteArray());
        }
        return output.toByteArray();
    }


    // Codificación Hamming para bloques de 4096 bits (simplificado)
    public static byte[] hamming4096Encode(byte[] data) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int totalBits  = 4096;
        final int r          = 13;                      // 12 control + 1 paridad  Sobran 3 bits
        final int dataBits   = totalBits - r;           // 4083 bits de datos
        final int blockBytes = (dataBits) / 8;       // 510 bytes de datos (4083? bits, se ignoran últimos bits faltantes)

        for (int i = 0; i < data.length; i += blockBytes) {
            int end   = Math.min(i + blockBytes, data.length);
            byte[] block = Arrays.copyOfRange(data, i, end);

            if (block.length < blockBytes) {
                byte[] padded = new byte[blockBytes];
                System.arraycopy(block, 0, padded, 0, block.length);
                block = padded;
            }

            boolean[] code = new boolean[totalBits];
            int dataBitIndex = 0;
            // 1) Datos
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if ((bitPos & (bitPos - 1)) != 0) {
                    int byteIdx = dataBitIndex / 8;
                    int bitIdx  = 7 - (dataBitIndex % 8);
                    boolean bit = false;
                    if (byteIdx < block.length) {
                        bit = ((block[byteIdx] >> bitIdx) & 1) == 1;
                    }
                    code[bitPos - 1] = bit;
                    dataBitIndex++;
                }
            }
            // 2) Bits de control
            for (int p = 0; p < r - 1; p++) {
                int pos = 1 << p;
                boolean parity = false;
                for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                    if (bitPos != pos && ((bitPos & pos) != 0)) {
                        parity ^= code[bitPos - 1];
                    }
                }
                code[pos - 1] = parity;
            }
            // 3) Paridad global
            int globalPos = 1 << (r - 1);
            boolean paridad = false;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if (bitPos != globalPos) {
                    paridad ^= code[bitPos - 1];
                }
            }
            code[globalPos - 1] = paridad;

            // 4) Packing
            for (int j = 0; j < totalBits; j += 8) {
                int val = 0;
                for (int k = 0; k < 8; k++) {
                    val = (val << 1) | (code[j + k] ? 1 : 0);
                }
                output.write(val);
            }
        }

        return output.toByteArray();
    }
    // Decodificación Hamming‑4096 con detección (pero sin corrección real)
    public static byte[] hamming4096Decode(byte[] encodedData) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int totalBits  = 4096;
        final int r          = 13;                     // 12 primarios + 1 global
        final int codeBytes  = totalBits / 8;          // 512 bytes por bloque
        final int dataBits   = totalBits - r;          // 4083 bits de datos
        final int blockBytes = (dataBits) / 8;     // 510 bytes de datos

        for (int i = 0; i < encodedData.length; i += codeBytes) {
            // 1) Reconstruir array de bits de 4096 posiciones
            boolean[] code = new boolean[totalBits];
            int limit = Math.min(codeBytes, encodedData.length - i);
            for (int j = 0; j < limit; j++) {
                int b = encodedData[i + j] & 0xFF;
                for (int k = 0; k < 8; k++) {
                    code[j * 8 + k] = ((b >> (7 - k)) & 1) == 1;
                }
            }

            // 2) Calcular síndrome (bits de paridad primarios)
            int syndrome = 0;
            for (int p = 0; p < r - 1; p++) {
                int mask = 1 << p;
                boolean parity = false;
                for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                    if ((bitPos & mask) != 0) parity ^= code[bitPos - 1];
                }
                if (parity) syndrome |= mask;
            }

            // 3) Calcular paridad global (incluye todos los bits del código)
            boolean paridad = false;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                paridad ^= code[bitPos - 1];
            }

            // 4) Corregir si hay un solo bit malo
            if (syndrome != 0) {
                if (paridad) {
                    // un solo error: invertimos ese bit
                    code[syndrome - 1] = !code[syndrome - 1];
                } else {
                    System.out.println("⚠️ Dos errores detectados en bloque " + (i / codeBytes));
                }
            }

            // 5) Extraer solo los bits de datos (posiciones que NO son potencias de 2)
            ByteArrayOutputStream blockOut = new ByteArrayOutputStream();
            int bitCount = 0, currByte = 0;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if ((bitPos & (bitPos - 1)) != 0) {
                    currByte = (currByte << 1) | (code[bitPos - 1] ? 1 : 0);
                    bitCount++;
                    if (bitCount % 8 == 0) {
                        blockOut.write(currByte);
                        currByte = 0;
                    }
                }
            }
            // 6) Escribir SOLO los bytes de datos esperados
            byte[] blockData = blockOut.toByteArray();
            output.write(blockData, 0, Math.min(blockData.length, blockBytes));
        }

        return output.toByteArray();
    }

    // Decodificación Hamming‑4096 SIN corrección: simplemente saca los bytes de datos
    public static byte[] hamming4096DecodeNoCorrect(byte[] encodedData) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int totalBits  = 4096;
        final int r          = 13;              // 12 paridad primarios + 1 global
        final int codeBytes  = totalBits / 8;   // 512 bytes por bloque
        final int dataBits   = totalBits - r;   // 4083 bits de datos
        final int blockBytes = dataBits / 8;    // 510 bytes de datos (los bits sobrantes se ignoran)

        for (int i = 0; i < encodedData.length; i += codeBytes) {
            // 1) Leer bloque de 512 bytes a array de bits
            boolean[] code = new boolean[totalBits];
            for (int j = 0; j < codeBytes; j++) {
                int b = encodedData[i + j] & 0xFF;
                for (int k = 0; k < 8; k++) {
                    code[j * 8 + k] = ((b >> (7 - k)) & 1) == 1;
                }
            }
            // 2) Extraer bits de datos (sin corrección)
            ByteArrayOutputStream blockOut = new ByteArrayOutputStream();
            int bitCount = 0, currByte = 0;
            for (int bitPos = 1; bitPos <= totalBits; bitPos++) {
                if ((bitPos & (bitPos - 1)) != 0) {
                    currByte = (currByte << 1) | (code[bitPos - 1] ? 1 : 0);
                    bitCount++;
                    if (bitCount % 8 == 0) {
                        blockOut.write(currByte);
                        currByte = 0;
                    }
                }
            }
            output.write(blockOut.toByteArray());
        }
        return output.toByteArray();
    }


    // Función auxiliar para calcular paridad (simplificado)
    private static byte calculateParity(byte[] block) {
        byte parity = 0;
        for (byte b : block) {
            parity ^= b;
        }
        return parity;
    }

    // Introducir errores aleatorios
    public static byte[] introduceErrors(byte[] data, int blockSize) {
        byte[] corrupted = Arrays.copyOf(data, data.length);
        Random random = new Random();

        System.out.println(blockSize + "\n");
        int blocks = data.length / (blockSize / 8);
        for (int i = 0; i < blocks; i++) {
            if (random.nextDouble() < 0.5) { // 50% de probabilidad de error por bloque
                int pos = i * blockSize + random.nextInt(blockSize);
                if (pos < corrupted.length) {
                    // Voltear un bit aleatorio
                    int bitPos = random.nextInt(8);
                    corrupted[pos] ^= (1 << bitPos);
                }
            }
        }

        return corrupted;
    }
}