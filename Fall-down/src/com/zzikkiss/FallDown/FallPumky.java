package com.zzikkiss.FallDown;



import static org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.hardware.SensorManager;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;


public class FallPumky extends BaseGameActivity implements IAccelerometerListener, IOnSceneTouchListener {
        // ===========================================================
        // Constants
        // ===========================================================

        private static final int CAMERA_WIDTH = 480;
        private static final int CAMERA_HEIGHT = 800;

        private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

        // ===========================================================
        // Fields
        // ===========================================================

        private Texture mTexture;

        private TiledTextureRegion mBoxFaceTextureRegion;
        private TiledTextureRegion mCircleFaceTextureRegion;
        private TiledTextureRegion mTriangleFaceTextureRegion;
        private TiledTextureRegion mHexagonFaceTextureRegion;

        private PhysicsWorld mPhysicsWorld;

        private int mFaceCount = 0;

        // ===========================================================
        // Constructors
        // ===========================================================

        // ===========================================================
        // Getter & Setter
        // ===========================================================

        // ===========================================================
        // Methods for/from SuperClass/Interfaces
        // ===========================================================

        @Override
        public Engine onLoadEngine() {
                Toast.makeText(this, "Fais tomber Pumky le plus bas possible", Toast.LENGTH_LONG).show();
                final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
                final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
                engineOptions.getTouchOptions().setRunOnUpdateThread(true);
                return new Engine(engineOptions);
        }

        @Override
        public void onLoadResources() {
                /* Textures. */
                this.mTexture = new Texture(64, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
                TextureRegionFactory.setAssetBasePath("gfx/");

                /* TextureRegions. */
                this.mCircleFaceTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mTexture, this, "face_circle_tiled.png", 0, 32, 2, 1); // 64x32
                this.mEngine.getTextureManager().loadTexture(this.mTexture);

                this.enableAccelerometerSensor(this);
        }

        @Override
        public Scene onLoadScene() {
                this.mEngine.registerUpdateHandler(new FPSLogger());

                final Scene scene = new Scene(2);
                scene.setBackground(new ColorBackground(0, 0,4, 0));
                scene.setOnSceneTouchListener(this);

                this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.MAGNETIC_FIELD_EARTH_MAX), false);

                final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
                final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
                final Shape left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
                final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);

                final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
                PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
                PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
                PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
                PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

                scene.getTopLayer().addEntity(ground);
                scene.getTopLayer().addEntity(roof);
                scene.getTopLayer().addEntity(left);
                scene.getTopLayer().addEntity(right);


                scene.registerUpdateHandler(this.mPhysicsWorld);

                return scene;
        }

        @Override
        public void onLoadComplete() {

        }

        @Override
        public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
                if(this.mPhysicsWorld != null) {
                        if(pSceneTouchEvent.getAction()==TouchEvent.ACTION_DOWN) {
                                this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                                return true;
                        }
                }
                return false;
        }

        @Override
        public void onAccelerometerChanged(final AccelerometerData pAccelerometerData) {
        		
        	this.mPhysicsWorld.setGravity(new Vector2(-pAccelerometerData.getX()*10,pAccelerometerData.getY()*10));
        }

        // ===========================================================
        // Methods
        // ===========================================================

        private void addFace(final float pX, final float pY) {
                final Scene scene = this.mEngine.getScene();

              
                final AnimatedSprite face;
                final Body body;

                face = new AnimatedSprite(pX, pY, this.mCircleFaceTextureRegion);
                body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
               

                face.animate(200);

                scene.getTopLayer().addEntity(face);
                this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true,false,false));
        }

        /**
         * Creates a {@link Body} based on a {@link PolygonShape} in the form of a triangle:
         * <pre>
         *  /\
         * /__\
         * </pre>
         */
        private static Body createTriangleBody(final PhysicsWorld pPhysicsWorld, final Shape pShape, final BodyType pBodyType, final FixtureDef pFixtureDef) {
                /* Remember that the vertices are relative to the center-coordinates of the Shape. */
                final float halfWidth = pShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
                final float halfHeight = pShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;

                final float top = -halfHeight;
                final float bottom = halfHeight;
                final float left = -halfHeight;
                final float centerX = 0;
                final float right = halfWidth;

                final Vector2[] vertices = {
                                new Vector2(centerX, top),
                                new Vector2(right, bottom),
                                new Vector2(left, bottom)
                };

                return PhysicsFactory.createPolygonBody(pPhysicsWorld, pShape, vertices, pBodyType, pFixtureDef);
        }

        /**
         * Creates a {@link Body} based on a {@link PolygonShape} in the form of a hexagon:
         * <pre>
         *  /\
         * /  \
         * |  |
         * |  |
         * \  /
         *  \/
         * </pre>
         */
        private static Body createHexagonBody(final PhysicsWorld pPhysicsWorld, final Shape pShape, final BodyType pBodyType, final FixtureDef pFixtureDef) {
                /* Remember that the vertices are relative to the center-coordinates of the Shape. */
                final float halfWidth = pShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
                final float halfHeight = pShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;

                /* The top and bottom vertex of the hexagon are on the bottom and top of hexagon-sprite. */
                final float top = -halfHeight;
                final float bottom = halfHeight;

                final float centerX = 0;

                /* The left and right vertices of the heaxgon are not on the edge of the hexagon-sprite, so we need to inset them a little. */
                final float left = -halfWidth + 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
                final float right = halfWidth - 2.5f / PIXEL_TO_METER_RATIO_DEFAULT;
                final float higher = top + 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;
                final float lower = bottom - 8.25f / PIXEL_TO_METER_RATIO_DEFAULT;

                final Vector2[] vertices = {
                                new Vector2(centerX, top),
                                new Vector2(right, higher),
                                new Vector2(right, lower),
                                new Vector2(centerX, bottom),
                                new Vector2(left, lower),
                                new Vector2(left, higher)
                };

                return PhysicsFactory.createPolygonBody(pPhysicsWorld, pShape, vertices, pBodyType, pFixtureDef);
        }

        // ===========================================================
        // Inner and Anonymous Classes
        // ===========================================================
}
