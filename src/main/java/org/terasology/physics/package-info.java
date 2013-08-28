/**
 * When using the physics package (obviously because you want to do something
 * with the physics), you should start looking at the PhysicsEngine class. This
 * interface should be available from the CoreRegistry, using
 * CoreRegistry.get(PhysicsEngine.class). From there most of the physics
 * behaviour can be accessed.
 * <p/>
 * Besides the PhysicsEngine and the interfaces and classes returned by it or
 * required by it, there are three other groups of classes.
 * <ul>
 * <li>The first groups contains the Component classes used by the
 * PhysicsEngine. This groups can be found in the "components" sub-package. The
 * Physics engine may require that entities have certain types of components for
 * certain behaviour. For example, to create a rigid body, you must provide the
 * engine with en entity that has a RigidBodyComponent and LocationComponent.
 * Components that are directly related to the physics can be found in this
 * sub-package.
 * <li>The second group contains the physics related events and the
 * PhysicsSystem class. This group can be found in the "events" sub-package. The
 * events themselves should be fairly obvious, however the use of the
 * PhysicsSystem class requires a bit of explaining. The PhysicsSystem class is
 * a bridge between the event system of Terasology and the PhysicsEngine
 * interface. It catches of any events that should alter the physics engine and
 * converts them into calls to the PhysicsEngine interface. When adding new
 * functionality to the PhysicsEngine class, it is a good idea to also take a
 * look at the PhysicsSystem class.
 * <li>The last group is the implementation of the various interfaces. At the
 * moment of writing this documentation only one implementation exists, the
 * bullet physics engine implementation that uses JBullet. This implementation
 * can be found in the "bullet" sub-package.
 * </ul>
 */
package org.terasology.physics;