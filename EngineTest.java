import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.*;
import org.lwjgl.*;
import org.lwjgl.input.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
//import org.newdawn.slick.opengl.*;
import java.nio.*;
import java.util.*;
import java.io.*;
import java.awt.image.*;
import java.awt.Color;
public class EngineTest
{
	static float[] vertecies = {-0.5f, 0.5f, 0f, -0.5f, -0.5f, 0f, 0.5f, -0.5f, 0f, 0.5f, 0.5f, 0f};
	static int[] indecies = {0, 1, 3, 3, 1, 2};
	static float[] texels = {0, 0, 0, 1, 1, 1, 1, 0};
	static ArrayList<MPD> devs = new ArrayList<MPD>();
	static boolean inUse = false;
	public static void main(String[] args) throws Exception
	{
		inUse = true;
		System.out.println("Hi.");
		Display.setDisplayMode(new DisplayMode(1920, 1080));
		Display.create();
		
		Loader loader = new Loader();
		StaticShader sshader = new StaticShader();
		Renderer renderer = new Renderer(sshader);
		
		RawModel model = loader.loadToVAO(vertecies, texels, indecies);
		BufferedImage img = new BufferedImage(640, 480, 3);
		
		ModelTexture texture = new ModelTexture(loader.loadTextureFromID(Texture.createFromBufferedImage(img)));
		TexturedModel texturedModel = new TexturedModel(model, texture);
		Entity entity = new Entity(texturedModel, new Vector3f(0, 0, -1), 0, 0, 0, 1);
		
		MPD dev = new MPD(entity.model.texture.id);
		new Thread(dev).start();
		devs.add(dev);
		Thread.sleep(500);
		
		Camera camera = new Camera();
		
		while (!Display.isCloseRequested())
		{
			camera.move();
			camera.rotate();
			renderer.prepare();
			sshader.start();
			Texture.checkModify();
			renderer.render(entity, sshader, camera);
			sshader.stop();
			Display.sync(120);
			Display.update();
		}
		System.out.println("Closing...");
		sshader.cleanAll();
		System.out.println("Cleaned up shaders.");
		loader.cleanAll();
		System.out.println("Cleaned up loader.");
		for (MPD d : devs)
		{
			System.out.println("\tMark dev for destruction.");
			d.exit = true;
		}
		System.out.println("Set dev destroy flags.");
		Display.destroy();
		System.out.println("Window destroyed.");
	}
}
class RawModel
{
	int vaoID;
	int vertexCount;
	public RawModel(int id, int vcount)
	{
		vaoID = id;
		vertexCount = vcount;
	}
}
class Loader
{
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();
	
	public RawModel loadToVAO(float[] positions, float[] texels, int[] indecies)
	{
		int vaoID = createVAO();
		bindIndeciesBuffer(indecies);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, texels);
		unbindVAO();
		return new RawModel(vaoID, indecies.length);
	}
	private int createVAO()
	{
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	private void storeDataInAttributeList(int attributeNumber, int coordSize, float[] data)
	{
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		glVertexAttribPointer(attributeNumber, coordSize, GL11.GL_FLOAT, false, 0, 0);
		glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	public void unbindVAO()
	{
		glBindVertexArray(0);
	}
	
	public void bindIndeciesBuffer(int[] indecies)
	{
		int vboID = glGenBuffers();
		vbos.add(vboID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indecies);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
	}
	
	public IntBuffer storeDataInIntBuffer(int[] data)
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	public FloatBuffer storeDataInFloatBuffer(float[] data)
	{
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	/*public int loadTexture(String fileName)
	{
		org.newdawn.slick.opengl.Texture texture = null;
		try
		{
			texture = TextureLoader.getTexture("PNG", new FileInputStream("res/" + fileName + ".png"));
		}
		catch (Exception e){e.printStackTrace();System.exit(0);}
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}*/
	
	public int loadTextureFromID(int id)
	{
		textures.add(id);
		return id;
	}
	
	public void cleanAll()
	{
		for (int i : vaos)
			glDeleteVertexArrays(i);
		for (int i : vbos)
			glDeleteBuffers(i);
		for (int i : textures)
			glDeleteTextures(i);
	}
}
class Renderer
{
	static final float FOV = 70;
	static final float NEAR_PLANE = 0.1f;
	static final float FAR_PLANE = 1000;
	Matrix4f projectionMatrix;
	
	public Renderer(StaticShader shader)
	{
		createProjectionMatrix();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}
	
	public void prepare()
	{
		glEnable(GL_DEPTH_TEST);
		glClearColor(1, 1, 1, 1);	// r g b a
		glClear(GL11.GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
	}
	
	public void render(Entity entity, StaticShader shader, Camera camera)
	{
		TexturedModel texturedModel = entity.model;
		RawModel model = texturedModel.model;
		glBindVertexArray(model.vaoID);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		Matrix4f transformationMatrix = MathUtils.createTransformationMatrix(entity.position, entity.rx, entity.ry, entity.rz, entity.scale);
		shader.loadTransformationMatrix(transformationMatrix);
		shader.loadViewMatrix(camera);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texturedModel.texture.id);
		glDrawElements(GL_TRIANGLES, model.vertexCount, GL_UNSIGNED_INT, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
	}
	
	public void createProjectionMatrix()
	{
		float aspectRatio = Display.getWidth()/(float)Display.getHeight();
		float y_scale = (float)((1/(float)Math.tan(Math.toRadians(FOV/2f)))*aspectRatio);
		float x_scale = y_scale/aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;
		
		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE)/frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2*NEAR_PLANE*FAR_PLANE)/frustum_length);
		projectionMatrix.m33 = 0;
	}
}
class StaticShader extends ShaderProgram
{
	private static final String VERTEX_SHADER = "shaders/vertexShader.vsh";
	private static final String FRAGMENT_SHADER = "shaders/fragmentShader.fsh";
	
	int loc_transMat;
	int loc_projMat;
	int loc_viewMat;
	
	public StaticShader()
	{
		super(VERTEX_SHADER, FRAGMENT_SHADER);
	}
	protected void bindAttributes()
	{
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "texels");
	}
	protected void getAllUniformLocations()
	{
		loc_transMat = super.getUniformLocation("transformationMatrix");
		loc_projMat = super.getUniformLocation("projectionMatrix");
		loc_viewMat = super.getUniformLocation("viewMatrix");
	}
	
	public void loadTransformationMatrix(Matrix4f matrix)
	{
		super.loadMatrix(loc_transMat, matrix);
	}
	public void loadProjectionMatrix(Matrix4f matrix)
	{
		super.loadMatrix(loc_projMat, matrix);
	}
	public void loadViewMatrix(Camera camera)
	{
		Matrix4f matrix = MathUtils.createViewMatrix(camera);
		super.loadMatrix(loc_viewMat, matrix);
	}
}
abstract class ShaderProgram
{
	int programID;
	int vertexShaderID;
	int fragmentShaderID;
	
	static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(4*4);
	
	public ShaderProgram(String vsh, String fsh)
	{
		vertexShaderID = loadShader(vsh, GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fsh, GL_FRAGMENT_SHADER);
		programID = glCreateProgram();
		glAttachShader(programID, vertexShaderID);
		glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		glLinkProgram(programID);
		glValidateProgram(programID);
		getAllUniformLocations();
	}
	public void start()
	{
		glUseProgram(programID);
	}
	public void stop()
	{
		glUseProgram(0);
	}
	public void cleanAll()
	{
		stop();
		glDetachShader(programID, vertexShaderID);
		glDetachShader(programID, fragmentShaderID);
		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);
		glDeleteProgram(programID);
	}
	protected abstract void getAllUniformLocations();
	protected int getUniformLocation(String name)
	{
		return glGetUniformLocation(programID, name);
	}
	protected void loadFloat(int location, float value)
	{
		glUniform1f(location, value);
	}
	protected void loadVector(int location, Vector3f vector)
	{
		glUniform3f(location, vector.x, vector.y, vector.z);
	}
	protected void loadBoolean(int location, boolean value)
	{
		float load = 0;
		if (value)
			load = 0xFF;
		glUniform1f(location, load);
	}
	protected void loadMatrix(int location, Matrix4f matrix)
	{
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4(location, false, matrixBuffer);
	}
	protected abstract void bindAttributes();
	protected void bindAttribute(int attrib, String vname)
	{
		glBindAttribLocation(programID, attrib, vname);
	}
	public static int loadShader(String file, int type)
	{
		String shaderSource = "";
		System.out.println("k: " + file);
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			while (line != null)
			{
				shaderSource += line + "\n";
				line = in.readLine();
			}
		}
		catch (Exception e){e.printStackTrace();System.exit(0);}
		System.out.println("tada");
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, shaderSource);
		glCompileShader(shaderID);
		
		return shaderID;
	}
}
class ModelTexture
{
	int id;
	
	public ModelTexture(int id)
	{
		this.id = id;
	}
}
class TexturedModel
{
	RawModel model;
	ModelTexture texture;
	public TexturedModel(RawModel m, ModelTexture t)
	{
		model = m;
		texture = t;
	}
}
class Texture
{
	static BufferedImage imgToUse;
	static int toModify = -1;
	static ByteBuffer buffer = null;
	public static int createFromBufferedImage(BufferedImage img)
	{
		buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);
        
		for(int y = 0; y < img.getHeight(); y++)
			for(int x = 0; x < img.getWidth(); x++)
			{
				Color c = new Color(img.getRGB(x, y));
				buffer.put((byte)c.getRed());
				buffer.put((byte)c.getGreen());
				buffer.put((byte)c.getBlue());
				buffer.put((byte)c.getAlpha());
			}
		
		buffer.flip();
		
		int textureID = glGenTextures(); //Generate texture ID
		glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		return textureID;
	}
	public static int modifyImage(int number, BufferedImage img)
	{
		buffer.clear();
        
		for(int y = 0; y < img.getHeight(); y++)
			for(int x = 0; x < img.getWidth(); x++)
			{
				Color c = new Color(img.getRGB(x, y));
				buffer.put((byte)c.getRed());
				buffer.put((byte)c.getGreen());
				buffer.put((byte)c.getBlue());
				buffer.put((byte)c.getAlpha());
			}
		
		buffer.flip();
		
		int textureID = number; //Generate texture ID
		//glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID
		
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		return textureID;
	}
	public static void modifyImageLater(int number, BufferedImage img)
	{
		toModify = number;
		imgToUse = img;
	}
	public static void checkModify()
	{
		if (toModify != -1)
		{
			modifyImage(toModify, imgToUse);
			toModify = -1;
		}
	}
}
class MathUtils
{
	public static Matrix4f createTransformationMatrix(Vector3f trans, float rx, float ry, float rz, float scale)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(trans, matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);
		return matrix;
	}
	public static Matrix4f createViewMatrix(Camera camera)
	{
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.rotate((float)Math.toRadians(camera.pitch), new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.rotate((float)Math.toRadians(camera.yaw), new Vector3f(0, 1, 0), matrix, matrix);
		//Matrix4f.rotate((float)Math.toRadians(camera.roll), new Vector3f(0, 0, 1), matrix, matrix);
		Vector3f movm = new Vector3f(-camera.position.x, -camera.position.y, -camera.position.z);
		Matrix4f.translate(movm, matrix, matrix);
		return matrix;
	}
}
class Entity
{
	TexturedModel model;
	Vector3f position;
	float rx, ry, rz;
	float scale;
	public Entity(TexturedModel m, Vector3f pos, float x, float y, float z, float s)
	{
		model = m;
		position = pos;
		rx = x;
		ry = y;
		rz = z;
		scale = s;
	}
	public void translateRelative(float dx, float dy, float dz)
	{
		position.x += dx;
		position.y += dy;
		position.z += dz;
	}
	public void rotateRelative(float dx, float dy, float dz)
	{
		rx += dx;
		ry += dy;
		rz += dz;
	}
}
class Camera
{
	Vector3f position = new Vector3f(0, 0, 0);
	float pitch, yaw, roll;
	static int center_x = Display.getWidth()/2;
	static int center_y = Display.getHeight()/2;
	
	public Camera()
	{
		Mouse.setCursorPosition(center_x, center_y);
	}
	
	public void move()
	{
		if (Mouse.isButtonDown(0))
			return;
		Vector3f mvmt = new Vector3f();
		if (Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			mvmt.z -= Math.cos(yaw/180*Math.PI)*0.02;
			mvmt.x += Math.sin(yaw/180*Math.PI)*0.02;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			mvmt.z += Math.cos(yaw/180*Math.PI)*0.02;
			mvmt.x -= Math.sin(yaw/180*Math.PI)*0.02;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			mvmt.x += Math.cos(yaw/180*Math.PI)*0.02;
			mvmt.z += Math.sin(yaw/180*Math.PI)*0.02;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			mvmt.x -= Math.cos(yaw/180*Math.PI)*0.02;
			mvmt.z -= Math.sin(yaw/180*Math.PI)*0.02;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
			mvmt.y += 0.02;
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			mvmt.y -= 0.02;
		position.x += mvmt.x;
		position.y += mvmt.y;
		position.z += mvmt.z;
	}
	
	public void rotate()
	{
		if (Keyboard.isKeyDown(Keyboard.KEY_PAUSE))
			return;
		pitch -= (Mouse.getY()-center_y)/10f;
		yaw += (Mouse.getX()-center_x)/10f;
		Mouse.setCursorPosition(center_x, center_y);
	}
}