package skyblockworld;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SkyblockWorld implements GLEventListener, KeyListener {

    // --- 1. CLASES INTERNAS ---
    class Block {
        float x, y, z;
        Texture texture;
        float minX, maxX, minY, maxY, minZ, maxZ;
        int type; 

        public Block(float x, float y, float z, Texture t) { this(x, y, z, t, 0); }
        public Block(float x, float y, float z, Texture t, int type) {
            this.x = x; this.y = y; this.z = z;
            this.texture = t; this.type = type;
            this.minX = x - 0.5f; this.maxX = x + 0.5f;
            this.minY = y - 0.5f; this.maxY = y + 0.5f;
            this.minZ = z - 0.5f; this.maxZ = z + 0.5f;
        }
    }

    class Particle {
        float x, y, z;
        float vx, vy, vz;
        float vida; 
        int tipo; 

        public Particle(float x, float y, float z, int tipo) {
            this.x = x; this.y = y; this.z = z;
            this.tipo = tipo;
            this.vida = 1.0f;
            Random r = new Random();
            if (tipo == 0) { // NIEVE
                this.vx = (r.nextFloat() - 0.5f) * 0.05f; this.vy = -0.05f - (r.nextFloat() * 0.05f); this.vz = (r.nextFloat() - 0.5f) * 0.05f;
            } else if (tipo == 1) { // ARENA
                this.vx = 0.15f + (r.nextFloat() * 0.1f); this.vy = -0.02f - (r.nextFloat() * 0.02f); this.vz = (r.nextFloat() - 0.5f) * 0.1f;
            } else if (tipo == 2) { // NETHER (MODIFICADO: Brasas subiendo)
                this.vx = (r.nextFloat() - 0.5f) * 0.02f; 
                this.vy = 0.04f + (r.nextFloat() * 0.03f); 
                this.vz = (r.nextFloat() - 0.5f) * 0.02f;
            } else if (tipo == 3) { // END
                this.vx = (r.nextFloat() - 0.5f) * 0.04f; this.vy = (r.nextFloat() - 0.5f) * 0.04f; this.vz = (r.nextFloat() - 0.5f) * 0.04f;
            }
        }
        boolean update() {
            x += vx; y += vy; z += vz;
            if (tipo == 1) vida -= 0.015f; else if (tipo == 2) vida -= 0.01f; else vida -= 0.008f; 
            return vida > 0;
        }
    }

    // --- CLASE DRAGON ---
    class Dragon {
        float angle = 0;
        float wingAngle = 0;
        float wingSpeed = 0.15f;
        
        float centerX = 0; float centerZ = -80; 
        float radius = 90; 

        void update() {
            angle += 0.005f; 
            if (angle > Math.PI * 2) angle -= Math.PI * 2;
            
            wingAngle += wingSpeed;
            if (wingAngle > 0.5f || wingAngle < -0.5f) wingSpeed *= -1;
        }

        void draw(GL2 gl) {
            float x = centerX + (float)Math.cos(angle) * radius;
            float z = centerZ + (float)Math.sin(angle) * radius;
            float y = 30 + (float)Math.sin(angle * 3) * 5; 

            gl.glPushMatrix();
            gl.glTranslatef(x, y, z);
            gl.glRotatef((float)Math.toDegrees(-angle), 0, 1, 0); 

            // CABEZA
            gl.glPushMatrix();
            gl.glTranslatef(0, 0, 3);
            gl.glScalef(1.5f, 1.0f, 2.0f);
            dibujarCuboColor(gl, 0,0,0, 0.1f, 0.1f, 0.1f, 1.0f); 
            // Ojos
            gl.glPushMatrix(); gl.glTranslatef(0.51f, 0.2f, 0.2f); gl.glScalef(0.1f, 0.3f, 0.5f);
            dibujarCuboColor(gl, 0,0,0, 0.8f, 0.2f, 1.0f, 1.0f); gl.glPopMatrix(); 
            gl.glPushMatrix(); gl.glTranslatef(-0.51f, 0.2f, 0.2f); gl.glScalef(0.1f, 0.3f, 0.5f);
            dibujarCuboColor(gl, 0,0,0, 0.8f, 0.2f, 1.0f, 1.0f); gl.glPopMatrix(); 
            gl.glPopMatrix();

            // CUERPO
            gl.glPushMatrix();
            gl.glTranslatef(0, 0, -1); // Ajuste ligero
            gl.glScalef(1.2f, 1.2f, 4.0f);
            dibujarCuboColor(gl, 0,0,0, 0.1f, 0.1f, 0.1f, 1.0f);
            gl.glPopMatrix();

            // ALAS
            gl.glPushMatrix();
            gl.glTranslatef(0.6f, 0.5f, 0);
            gl.glRotatef((float)Math.toDegrees(wingAngle), 0, 0, 1);
            gl.glTranslatef(2.5f, 0, 0); 
            gl.glScalef(5.0f, 0.2f, 3.0f);
            dibujarCuboColor(gl, 0,0,0, 0.15f, 0.15f, 0.15f, 1.0f);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslatef(-0.6f, 0.5f, 0);
            gl.glRotatef(-(float)Math.toDegrees(wingAngle), 0, 0, 1);
            gl.glTranslatef(-2.5f, 0, 0);
            gl.glScalef(5.0f, 0.2f, 3.0f);
            dibujarCuboColor(gl, 0,0,0, 0.15f, 0.15f, 0.15f, 1.0f);
            gl.glPopMatrix();

            // COLA
            gl.glPushMatrix();
            gl.glTranslatef(0, 0, -4.5f);
            gl.glScalef(0.8f, 0.8f, 3.0f);
            dibujarCuboColor(gl, 0,0,0, 0.1f, 0.1f, 0.1f, 1.0f);
            gl.glPopMatrix();

            gl.glPopMatrix();
        }
    }

    private GLU glu;
    private List<Block> mundo = new ArrayList<>();
    private List<Particle> particulas = new ArrayList<>(); 
    private Random random = new Random();
    private Dragon enderDragon = new Dragon(); 

    private float[] posSol = {0.0f, 50.0f, 50.0f, 1.0f}; 

    private float rotacionDiamante = 0f; 
    private float camX = 0f, camY = 5.0f, camZ = 0f; 
    private float camYaw = 0f, camPitch = 0f;
    private boolean[] keys = new boolean[256];
    private float velocidadY = 0f;
    private float gravedad = 0.015f;
    private float fuerzaSalto = 0.28f; 
    private float velocidadCaminar = 0.15f; 
    private boolean enSuelo = false, volando = false, teclaSaltoPresionada = false; 
    private float playerWidth = 0.3f, playerHeight = 1.8f; 

    private Texture tPasto, tTierra, tNieve, tArena, tMadera, tHojas, tAgua, tCactus, tDiamante;
    private Texture tNether, tEnd, tObsidiana; 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GLProfile profile = GLProfile.get(GLProfile.GL2);
            GLCapabilities capabilities = new GLCapabilities(profile);
            capabilities.setDepthBits(24);
            GLJPanel canvas = new GLJPanel(capabilities);
            
            SkyblockWorld app = new SkyblockWorld();
            
            // --- REPRODUCIR MUSICA ---
            app.reproducirMusica("C418 - Moog City - Minecraft Volume Alpha.wav");
            
            canvas.addGLEventListener(app);
            canvas.addKeyListener(app);
            canvas.setFocusable(true);
            canvas.requestFocusInWindow();
            JFrame frame = new JFrame("Skyblock Final: Ender Dragon");
            frame.getContentPane().add(canvas);
            frame.setSize(1024, 768);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            FPSAnimator animator = new FPSAnimator(canvas, 60, true);
            animator.start();
        });
    }

    // --- METODO DE AUDIO ---
    public void reproducirMusica(String nombreArchivo) {
        try {
            File archivo = new File(nombreArchivo);
            if (!archivo.exists()) {
                archivo = new File("src/" + nombreArchivo);
            }

            if (archivo.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(archivo);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                
                // Control de volumen (opcional, para que no suene muy fuerte)
                try {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(-10.0f); // Reducir volumen en decibeles
                } catch (Exception e) {
                    // Ignorar si el control de volumen no está soportado
                }

                clip.loop(Clip.LOOP_CONTINUOUSLY); // Repetir
                clip.start();
            } else {
                System.err.println("ADVERTENCIA: No se encontró el archivo de audio: " + nombreArchivo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al reproducir audio. Asegúrate de que sea un archivo .wav válido (PCM 16 bit).");
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL2.GL_NORMALIZE); 
        
        gl.glEnable(GL2.GL_FOG);
        gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
        gl.glHint(GL2.GL_FOG_HINT, GL2.GL_NICEST);

        try {
            tPasto = cargarTextura(gl, "pasto.jpg"); tTierra = cargarTextura(gl, "tierra.jpg");
            tNieve = cargarTextura(gl, "nieve.jpg"); tArena = cargarTextura(gl, "arena.jpg");
            tMadera = cargarTextura(gl, "madera.jpg"); tHojas = cargarTextura(gl, "hojas.jpg");
            tAgua = cargarTextura(gl, "agua.jpg"); tCactus = cargarTextura(gl, "cactus.jpg");
            tDiamante = cargarTextura(gl, "diamante.jpg"); tNether = cargarTextura(gl, "netherblock.jpg");
            tEnd = cargarTextura(gl, "end.jpg"); tObsidiana = cargarTextura(gl, "obsidiana.jpg");
        } catch (Exception e) { System.err.println("Error texturas: " + e.getMessage()); }
        
        setupLuces(gl); 
        generarNivelParkour();
    }
    
    private void setupLuces(GL2 gl) {
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 1f}, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[]{1.0f, 1.0f, 0.9f, 1f}, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[]{1.0f, 1.0f, 1.0f, 1f}, 0);
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 50.0f);
        gl.glShadeModel(GL2.GL_SMOOTH);
    }

    private void generarNivelParkour() {
        mundo.clear();
        
        // 1. ISLA INICIO
        crearIslaRobusta(0, 0, 0, 9, 9, tPasto, tTierra, tTierra); 
        crearIslaRobusta(0, 1, 0, 5, 5, tPasto, tTierra, tTierra);
        crearArbol(-2, 2, -2); crearArbol(2, 2, 2);
        
        // -> Tropical
        mundo.add(new Block(0, 1, -6, tMadera)); 
        mundo.add(new Block(0, 2, -9, tMadera));
        mundo.add(new Block(0, 2, -13, tMadera));
        mundo.add(new Block(0, 2, -17, tMadera));

        // 2. ISLA TROPICAL 
        int tropX = 0; int tropZ = -28; 
        crearIslaRobusta(tropX, 1, tropZ, 13, 13, tArena, tArena, tArena);
        for(int x = tropX-4; x <= tropX+4; x++) for(int z = tropZ-4; z <= tropZ+4; z++) mundo.add(new Block(x, 2, z, tAgua));
        crearPalmera(tropX-5, 2, tropZ-5); crearPalmera(tropX+5, 2, tropZ+5); crearPalmera(tropX+5, 2, tropZ-5);

        // -> Desierto
        mundo.add(new Block(8, 3, -30, tHojas)); 
        mundo.add(new Block(12, 4, -34, tHojas));
        mundo.add(new Block(15, 5, -38, tHojas)); 
        mundo.add(new Block(18, 6, -42, tHojas));

        // 3. ISLA DESIERTO
        int desX = 22; int desZ = -50;
        crearIslaRobusta(desX, 6, desZ, 11, 11, tArena, tArena, tArena); 
        crearIslaRobusta(desX+2, 7, desZ-2, 6, 6, tArena, tArena, tArena);
        crearIslaRobusta(desX+3, 8, desZ-3, 3, 3, tArena, tArena, tArena);
        crearCactus(desX-4, 7, desZ+4); crearCactus(desX+4, 7, desZ+4); crearCactus(desX+3, 9, desZ-3); 
        
        // -> Nieve
        mundo.add(new Block(18, 7, -57, tTierra));
        mundo.add(new Block(14, 9, -60, tTierra)); 
        mundo.add(new Block(10, 8, -64, tTierra));
        mundo.add(new Block(6, 9, -68, tTierra));
        
        // 4. ISLA NIEVE
        int nieX = 0; int nieZ = -78;
        crearIslaRobusta(nieX, 9, nieZ, 12, 12, tNieve, tNieve, tNieve);
        crearIslaRobusta(nieX, 10, nieZ+2, 8, 6, tNieve, tNieve, tNieve);
        dibujarIglu(nieX, 11, nieZ+2); crearCalabaza(nieX+4, 11, nieZ); crearCalabaza(nieX-4, 11, nieZ);
        
        // -> Nether
        mundo.add(new Block(-8, 10, -82, tNether)); 
        mundo.add(new Block(-13, 9, -86, tNether)); 
        mundo.add(new Block(-18, 9, -90, tNether));
        mundo.add(new Block(-22, 10, -94, tNether));
        
        // 5. ISLA NETHER
        int nethX = -28; int nethZ = -105;
        crearIslaRobusta(nethX, 10, nethZ, 14, 14, tNether, tNether, tObsidiana);
        for(int x=nethX-3; x<=nethX+3; x++) for(int z=nethZ-3; z<=nethZ+3; z++) if(random.nextFloat() > 0.3f) mundo.add(new Block(x, 10, z, null, 2)); 
        crearIslaRobusta(nethX, 11, nethZ, 6, 4, tNether, tNether, tObsidiana);
        crearPortalNether(nethX - 2, 12, nethZ); 
        
        // -> End (MODIFICADO: Ruta más fácil con más bloques)
        mundo.add(new Block(-24, 11, -114, tObsidiana)); 
        mundo.add(new Block(-21, 11.2f, -117, tObsidiana));
        mundo.add(new Block(-18, 11.5f, -120, tObsidiana));
        mundo.add(new Block(-15, 12, -123, tObsidiana));  
        mundo.add(new Block(-12, 12.5f, -126, tObsidiana)); 
        mundo.add(new Block(-9, 13, -129, tObsidiana)); 
        mundo.add(new Block(-6, 13.5f, -132, tObsidiana)); 
        mundo.add(new Block(-3, 14, -136, tObsidiana));
        mundo.add(new Block(0, 14.5f, -140, tObsidiana));
        mundo.add(new Block(0, 15, -144, tObsidiana)); 
        mundo.add(new Block(0, 15, -147, tObsidiana)); // LLegada a la isla
        
        // 6. ISLA END
        int endX = 0; int endZ = -155;
        crearIslaRobusta(endX, 15, endZ, 18, 18, tEnd, tEnd, tObsidiana);
        for(int i=0; i<30; i++) mundo.add(new Block(endX + random.nextInt(14)-7, 16, endZ + random.nextInt(14)-7, tEnd));
        crearPilarObsidiana(endX-7, 16, endZ-7, 7); 
        crearPilarObsidiana(endX+7, 16, endZ+7, 8); 
        crearPilarObsidiana(endX+7, 16, endZ-6, 6); 
        crearPilarObsidiana(endX-7, 16, endZ+7, 9); 
        
        mundo.add(new Block(endX, 16, endZ, tObsidiana)); 
    }

    private void updateParticles() {
        if (particulas.size() < 8000) { 
            for(int i=0; i<10; i++) particulas.add(new Particle(0 + (random.nextFloat()*24-12), 20 + random.nextFloat()*5, -78 + (random.nextFloat()*24-12), 0)); 
            for(int i=0; i<12; i++) particulas.add(new Particle(22 + (random.nextFloat()*20-10), 10 + random.nextFloat()*10, -50 + (random.nextFloat()*20-10), 1)); 
            
            // NETHER MODIFICADO: Generar desde el suelo (Y=10.1) hacia arriba
            for(int i=0; i<10; i++) {
                particulas.add(new Particle(
                    -28 + (random.nextFloat()*20-10),  
                    10.1f + random.nextFloat() * 1.0f, // Desde el piso
                    -105 + (random.nextFloat()*20-10), 
                    2
                )); 
            }
            
            for(int i=0; i<10; i++) particulas.add(new Particle(0 + (random.nextFloat()*30-15), 20 + random.nextFloat()*10, -155 + (random.nextFloat()*30-15), 3)); 
        }
        Iterator<Particle> it = particulas.iterator();
        while (it.hasNext()) { if (!it.next().update()) it.remove(); }
    }

    private void updateAtmosphere(GL2 gl) {
        float[] fogColor;
        float densityStart = 40f; float densityEnd = 140f; 

        if (camZ > -15) { 
            gl.glClearColor(0.6f, 0.8f, 1.0f, 1.0f); fogColor = new float[]{0.6f, 0.8f, 1.0f, 1.0f};
        } else if (camZ > -40) { 
            gl.glClearColor(0.5f, 0.9f, 1.0f, 1.0f); fogColor = new float[]{0.5f, 0.9f, 1.0f, 1.0f};
        } else if (camZ > -65) { 
            gl.glClearColor(0.85f, 0.75f, 0.5f, 1.0f); fogColor = new float[]{0.85f, 0.75f, 0.5f, 1.0f}; densityStart = 10f; densityEnd = 50f;
        } else if (camZ > -90) { 
            gl.glClearColor(0.9f, 0.95f, 1.0f, 1.0f); fogColor = new float[]{0.9f, 0.95f, 1.0f, 1.0f}; densityStart = 10f; densityEnd = 40f;
        } else if (camZ > -130) { 
            gl.glClearColor(0.3f, 0.0f, 0.0f, 1.0f); fogColor = new float[]{0.2f, 0.0f, 0.0f, 1.0f}; densityStart = 20f; densityEnd = 70f;
        } else { 
            gl.glClearColor(0.1f, 0.0f, 0.2f, 1.0f); fogColor = new float[]{0.1f, 0.0f, 0.15f, 1.0f}; densityStart = 30f; densityEnd = 100f;
        }
        gl.glFogfv(GL2.GL_FOG_COLOR, fogColor, 0); gl.glFogf(GL2.GL_FOG_START, densityStart); gl.glFogf(GL2.GL_FOG_END, densityEnd);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        updatePhysics();
        updateParticles(); 
        enderDragon.update(); 
        
        GL2 gl = drawable.getGL().getGL2();
        updateAtmosphere(gl);
        
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        // 1. CAMARA
        float lookX = camX + (float) Math.sin(Math.toRadians(camYaw));
        float lookY = camY + (float) Math.tan(Math.toRadians(camPitch));
        float lookZ = camZ - (float) Math.cos(Math.toRadians(camYaw));
        glu.gluLookAt(camX, camY+playerHeight-0.2f, camZ, lookX, lookY+playerHeight-0.2f, lookZ, 0, 1, 0);

        // 2. SOL
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, posSol, 0);
        gl.glDisable(GL2.GL_LIGHTING); 
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glPushMatrix();
        gl.glTranslatef(posSol[0], posSol[1], posSol[2]);
        gl.glScalef(5.0f, 5.0f, 5.0f); 
        dibujarCuboColor(gl, 0, 0, 0, 1.0f, 1.0f, 0.0f, 1.0f); 
        gl.glPopMatrix();
        gl.glEnable(GL2.GL_LIGHTING); 

        // 3. MUNDO
        gl.glEnable(GL2.GL_TEXTURE_2D);
        for(Block b : mundo) if (b.type == 0) dibujarCubo(gl, b.x, b.y, b.z, b.texture);
        gl.glDisable(GL2.GL_TEXTURE_2D);

        // 4. DRAGÓN
        gl.glDisable(GL2.GL_TEXTURE_2D);
        enderDragon.draw(gl);

        // 5. TRANSPARENCIAS
        gl.glEnable(GL2.GL_BLEND);
        gl.glDepthMask(false);
        for(Block b : mundo) {
             if (b.type == 1) dibujarCuboColor(gl, b.x, b.y, b.z, 0.6f, 0.0f, 0.8f, 0.7f); 
             else if (b.type == 2) dibujarCuboColor(gl, b.x, b.y, b.z, 1.0f, 0.4f, 0.0f, 0.8f); 
        }
        gl.glDepthMask(true); 
        
        // 6. TALLO
        for(Block b : mundo) if (b.type == 3) { 
             gl.glPushMatrix(); gl.glTranslatef(b.x, b.y-0.2f, b.z); gl.glScalef(0.3f, 0.4f, 0.3f);
             dibujarCuboColor(gl, 0, 0, 0, 0.1f, 0.6f, 0.1f, 1.0f); gl.glPopMatrix();
        }

        // 7. DIAMANTE
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glPushMatrix(); gl.glTranslatef(0f, 18f, -155f); gl.glRotatef(rotacionDiamante, 0f, 1f, 0f); 
        gl.glRotatef(20f, 1f, 0f, 1f); gl.glScalef(1.5f, 1.5f, 1.5f); 
        dibujarCubo(gl, 0, 0, 0, tDiamante); gl.glPopMatrix();
        rotacionDiamante += 2.0f;

        // --- 8. PARTÍCULAS ---
        gl.glDisable(GL2.GL_LIGHTING);        
        gl.glDisable(GL2.GL_TEXTURE_2D);       
        
        for(Particle p : particulas) {
            gl.glPushMatrix();
            gl.glTranslatef(p.x, p.y, p.z);
            if(p.tipo == 0) { // Nieve
                gl.glScalef(0.12f, 0.12f, 0.12f); dibujarCuboColor(gl, 0,0,0, 1.0f, 1.0f, 1.0f, 1.0f); 
            } else if(p.tipo == 1) { // Arena
                gl.glScalef(0.12f, 0.12f, 0.12f); dibujarCuboColor(gl, 0,0,0, 1.0f, 0.8f, 0.0f, 1.0f); 
            } else if(p.tipo == 2) { // Nether
                gl.glScalef(0.1f, 0.1f, 0.1f); dibujarCuboColor(gl, 0,0,0, 0.9f, 0.3f, 0.1f, 1.0f); 
            } else if(p.tipo == 3) { // End
                gl.glScalef(0.1f, 0.1f, 0.1f); dibujarCuboColor(gl, 0,0,0, 0.8f, 0.2f, 1.0f, 1.0f); 
            }
            gl.glPopMatrix();
        }
        gl.glEnable(GL2.GL_LIGHTING); 

        // RESPAWN SEGURO (0, 5, 0)
        if(camY < -40) { camX = 0; camY = 5; camZ = 0; velocidadY = 0; camYaw = 0; volando = false; } 
    }

    // --- UTILS ---
    private void crearPortalNether(int x, int y, int z) {
        for(int i=0; i<4; i++) { mundo.add(new Block(x+i, y, z, tObsidiana)); mundo.add(new Block(x+i, y+4, z, tObsidiana)); }
        for(int i=1; i<4; i++) { mundo.add(new Block(x, y+i, z, tObsidiana)); mundo.add(new Block(x+3, y+i, z, tObsidiana)); }
        for(int i=1; i<4; i++) for(int j=1; j<3; j++) mundo.add(new Block(x+j, y+i, z, null, 1));
    }
    private void crearCalabaza(int x, int y, int z) { mundo.add(new Block(x, y, z, null, 2)); mundo.add(new Block(x, y+1, z, null, 3)); }
    private void dibujarCubo(GL2 gl, float x, float y, float z, Texture tex) {
        if(tex != null) { tex.bind(gl); gl.glColor3f(1f, 1f, 1f); } else gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glPushMatrix(); gl.glTranslatef(x, y, z); gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0,0,1); gl.glTexCoord2f(0,0); gl.glVertex3f(-0.5f,-0.5f,0.5f); gl.glTexCoord2f(1,0); gl.glVertex3f(0.5f,-0.5f,0.5f); gl.glTexCoord2f(1,1); gl.glVertex3f(0.5f,0.5f,0.5f); gl.glTexCoord2f(0,1); gl.glVertex3f(-0.5f,0.5f,0.5f);
        gl.glNormal3f(0,0,-1); gl.glTexCoord2f(1,0); gl.glVertex3f(-0.5f,-0.5f,-0.5f); gl.glTexCoord2f(1,1); gl.glVertex3f(-0.5f,0.5f,-0.5f); gl.glTexCoord2f(0,1); gl.glVertex3f(0.5f,0.5f,-0.5f); gl.glTexCoord2f(0,0); gl.glVertex3f(0.5f,-0.5f,-0.5f);
        gl.glNormal3f(-1,0,0); gl.glTexCoord2f(0,0); gl.glVertex3f(-0.5f,-0.5f,-0.5f); gl.glTexCoord2f(1,0); gl.glVertex3f(-0.5f,-0.5f,0.5f); gl.glTexCoord2f(1,1); gl.glVertex3f(-0.5f,0.5f,0.5f); gl.glTexCoord2f(0,1); gl.glVertex3f(-0.5f,0.5f,-0.5f);
        gl.glNormal3f(1,0,0); gl.glTexCoord2f(1,0); gl.glVertex3f(0.5f,-0.5f,-0.5f); gl.glTexCoord2f(1,1); gl.glVertex3f(0.5f,0.5f,-0.5f); gl.glTexCoord2f(0,1); gl.glVertex3f(0.5f,0.5f,0.5f); gl.glTexCoord2f(0,0); gl.glVertex3f(0.5f,-0.5f,0.5f);
        gl.glNormal3f(0,1,0); gl.glTexCoord2f(0,1); gl.glVertex3f(-0.5f,0.5f,-0.5f); gl.glTexCoord2f(0,0); gl.glVertex3f(-0.5f,0.5f,0.5f); gl.glTexCoord2f(1,0); gl.glVertex3f(0.5f,0.5f,0.5f); gl.glTexCoord2f(1,1); gl.glVertex3f(0.5f,0.5f,-0.5f);
        gl.glNormal3f(0,-1,0); gl.glTexCoord2f(1,1); gl.glVertex3f(-0.5f,-0.5f,-0.5f); gl.glTexCoord2f(0,1); gl.glVertex3f(0.5f,-0.5f,-0.5f); gl.glTexCoord2f(0,0); gl.glVertex3f(0.5f,-0.5f,0.5f); gl.glTexCoord2f(1,0); gl.glVertex3f(-0.5f,-0.5f,0.5f);
        gl.glEnd(); gl.glPopMatrix();
    }
    private void dibujarCuboColor(GL2 gl, float x, float y, float z, float r, float g, float b, float a) {
        gl.glColor4f(r, g, b, a); gl.glPushMatrix(); gl.glTranslatef(x, y, z); gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3f(-0.5f,-0.5f,0.5f); gl.glVertex3f(0.5f,-0.5f,0.5f); gl.glVertex3f(0.5f,0.5f,0.5f); gl.glVertex3f(-0.5f,0.5f,0.5f); 
        gl.glVertex3f(-0.5f,-0.5f,-0.5f); gl.glVertex3f(-0.5f,0.5f,-0.5f); gl.glVertex3f(0.5f,0.5f,-0.5f); gl.glVertex3f(0.5f,-0.5f,-0.5f); 
        gl.glVertex3f(-0.5f,-0.5f,-0.5f); gl.glVertex3f(0.5f,-0.5f,-0.5f); gl.glVertex3f(0.5f,-0.5f,0.5f); gl.glVertex3f(-0.5f,-0.5f,0.5f); 
        gl.glVertex3f(-0.5f,0.5f,-0.5f); gl.glVertex3f(-0.5f,0.5f,0.5f); gl.glVertex3f(0.5f,0.5f,0.5f); gl.glVertex3f(0.5f,0.5f,-0.5f); 
        gl.glVertex3f(-0.5f,-0.5f,-0.5f); gl.glVertex3f(-0.5f,-0.5f,0.5f); gl.glVertex3f(-0.5f,0.5f,0.5f); gl.glVertex3f(-0.5f,0.5f,-0.5f); 
        gl.glVertex3f(0.5f,-0.5f,-0.5f); gl.glVertex3f(0.5f,0.5f,-0.5f); gl.glVertex3f(0.5f,0.5f,0.5f); gl.glVertex3f(0.5f,-0.5f,0.5f); 
        gl.glEnd(); gl.glPopMatrix();
    }
    private void crearIslaRobusta(int cx, int cy, int cz, int w, int d, Texture top, Texture filler, Texture bottom) {
        int startX = cx - w/2; int startZ = cz - d/2;
        for(int x=startX; x<startX+w; x++) for(int z=startZ; z<startZ+d; z++) mundo.add(new Block(x, cy, z, top));
        for(int i=1; i<=2; i++) for(int x=startX+i; x<startX+w-i; x++) for(int z=startZ+i; z<startZ+d-i; z++) mundo.add(new Block(x, cy-i, z, filler));
        int tipOffset = 3; for(int x=startX+tipOffset; x<startX+w-tipOffset; x++) for(int z=startZ+tipOffset; z<startZ+d-tipOffset; z++) mundo.add(new Block(x, cy-3, z, bottom));
    }
    private void crearPilarObsidiana(int x, int y, int z, int altura) { for(int i=0; i<altura; i++) mundo.add(new Block(x, y+i, z, tObsidiana)); }
    private void crearPalmera(int x, int y, int z) {
        mundo.add(new Block(x, y, z, tMadera)); mundo.add(new Block(x+1, y+1, z, tMadera)); mundo.add(new Block(x+2, y+2, z, tMadera)); mundo.add(new Block(x+2, y+3, z, tMadera));
        int topX = x+2; int topY = y+4;
        mundo.add(new Block(topX, topY, z, tHojas)); mundo.add(new Block(topX+1, topY, z, tHojas)); mundo.add(new Block(topX-1, topY, z, tHojas));
        mundo.add(new Block(topX, topY, z+1, tHojas)); mundo.add(new Block(topX, topY, z-1, tHojas)); mundo.add(new Block(topX+2, topY-1, z, tHojas)); mundo.add(new Block(topX-2, topY-1, z, tHojas));
    }
    private void crearArbol(int x, int y, int z) {
        for(int i=0; i<3; i++) mundo.add(new Block(x, y+i, z, tMadera));
        for(int dx=-1; dx<=1; dx++) for(int dz=-1; dz<=1; dz++) if(dx!=0 || dz!=0) mundo.add(new Block(x+dx, y+2, z+dz, tHojas));
        mundo.add(new Block(x, y+3, z, tHojas));
    }
    private void crearCactus(int x, int y, int z) { mundo.add(new Block(x, y, z, tCactus)); mundo.add(new Block(x, y+1, z, tCactus)); }
    private void dibujarIglu(int x, int y, int z) {
        for(int dx=-1; dx<=1; dx++) for(int dz=-1; dz<=1; dz++) mundo.add(new Block(x+dx, y, z+dz, tNieve));
        mundo.add(new Block(x, y+1, z, tNieve)); mundo.add(new Block(x, y, z+2, tNieve));
    }
    private Texture cargarTextura(GL2 gl, String nombre) {
        try { File f = new File(nombre); if (!f.exists()) f = new File("src/" + nombre); if (!f.exists()) return null;
            BufferedImage img = ImageIO.read(f); Texture t = AWTTextureIO.newTexture(GLProfile.getDefault(), img, false);
            t.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST); t.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
            return t; } catch (Exception e) { return null; }
    }
    private void updatePhysics() {
        if (keys[KeyEvent.VK_SPACE]) { if (!teclaSaltoPresionada) { teclaSaltoPresionada = true; if (enSuelo) { velocidadY = fuerzaSalto; enSuelo = false; } else { volando = !volando; velocidadY = 0; } } } else { teclaSaltoPresionada = false; }
        if (volando) { if (keys[KeyEvent.VK_SPACE]) camY += 0.2f; if (keys[KeyEvent.VK_SHIFT]) camY -= 0.2f; velocidadY = 0; }
        float radYaw = (float) Math.toRadians(camYaw); float dx = 0, dz = 0;
        if(keys[KeyEvent.VK_W]) { dx += Math.sin(radYaw); dz -= Math.cos(radYaw); } if(keys[KeyEvent.VK_S]) { dx -= Math.sin(radYaw); dz += Math.cos(radYaw); }
        if(keys[KeyEvent.VK_A]) { dx -= Math.cos(radYaw); dz -= Math.sin(radYaw); } if(keys[KeyEvent.VK_D]) { dx += Math.cos(radYaw); dz += Math.sin(radYaw); }
        float speed = volando ? 0.3f : velocidadCaminar; if(dx != 0 || dz != 0) { float length = (float) Math.sqrt(dx*dx + dz*dz); dx = (dx / length) * speed; dz = (dz / length) * speed; }
        if(!checkCollision(camX + dx, camY, camZ)) camX += dx; if(!checkCollision(camX, camY, camZ + dz)) camZ += dz;
        if (!volando) { velocidadY -= gravedad; float nextY = camY + velocidadY; boolean colisionVertical = false; float pMinX = camX - playerWidth; float pMaxX = camX + playerWidth; float pMinZ = camZ - playerWidth; float pMaxZ = camZ + playerWidth; float pMinY = nextY; float pMaxY = nextY + playerHeight; 
            for (Block b : mundo) { if (pMinX < b.maxX && pMaxX > b.minX && pMinZ < b.maxZ && pMaxZ > b.minZ && pMinY < b.maxY && pMaxY > b.minY) { colisionVertical = true; if (velocidadY < 0) { enSuelo = true; velocidadY = 0; camY = b.maxY; } else if (velocidadY > 0) { velocidadY = 0; camY = b.minY - playerHeight - 0.01f; } break; } }
            if (!colisionVertical) { camY = nextY; enSuelo = false; }
        }
        if(keys[KeyEvent.VK_LEFT]) camYaw -= 3f; if(keys[KeyEvent.VK_RIGHT]) camYaw += 3f; if(keys[KeyEvent.VK_UP] && camPitch < 90) camPitch += 3f; if(keys[KeyEvent.VK_DOWN] && camPitch > -90) camPitch -= 3f;
    }
    private boolean checkCollision(float nextX, float nextY, float nextZ) { if (volando) return false; float pMinX = nextX - playerWidth; float pMaxX = nextX + playerWidth; float pMinY = nextY; float pMaxY = nextY + playerHeight; float pMinZ = nextZ - playerWidth; float pMaxZ = nextZ + playerWidth; for (Block b : mundo) { if (pMinX < b.maxX && pMaxX > b.minX && pMinY < b.maxY && pMaxY > b.minY && pMinZ < b.maxZ && pMaxZ > b.minZ) return true; } return false; }
    @Override public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { GL2 gl = drawable.getGL().getGL2(); if (height <= 0) height = 1; float aspect = (float) width / height; gl.glViewport(0, 0, width, height); gl.glMatrixMode(GL2.GL_PROJECTION); gl.glLoadIdentity(); glu.gluPerspective(60.0, aspect, 0.1, 200.0); gl.glMatrixMode(GL2.GL_MODELVIEW); gl.glLoadIdentity(); }
    @Override public void dispose(GLAutoDrawable drawable) {}
    @Override public void keyPressed(KeyEvent e) { if(e.getKeyCode() < 256) keys[e.getKeyCode()] = true; }
    @Override public void keyReleased(KeyEvent e) { if(e.getKeyCode() < 256) keys[e.getKeyCode()] = false; }
    @Override public void keyTyped(KeyEvent e) {}
}