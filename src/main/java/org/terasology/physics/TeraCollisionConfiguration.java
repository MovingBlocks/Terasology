/*
 * Copyright 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.physics;

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.narrowphase.ConvexPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.GjkEpaPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.VoronoiSimplexSolver;

import static com.bulletphysics.collision.broadphase.BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE;
import static com.bulletphysics.collision.broadphase.BroadphaseNativeType.SPHERE_SHAPE_PROXYTYPE;
import static com.bulletphysics.collision.broadphase.BroadphaseNativeType.STATIC_PLANE_PROXYTYPE;

/**
 * @author Immortius
 */
public class TeraCollisionConfiguration extends CollisionConfiguration {

    //default simplex/penetration depth solvers
    protected VoronoiSimplexSolver simplexSolver;
    protected ConvexPenetrationDepthSolver pdSolver;

    //default CreationFunctions, filling the m_doubleDispatch table
    protected CollisionAlgorithmCreateFunc convexConvexCreateFunc;
    protected CollisionAlgorithmCreateFunc convexConcaveCreateFunc;
    protected CollisionAlgorithmCreateFunc swappedConvexConcaveCreateFunc;
    protected CollisionAlgorithmCreateFunc compoundCreateFunc;
    protected CollisionAlgorithmCreateFunc swappedCompoundCreateFunc;
    protected CollisionAlgorithmCreateFunc emptyCreateFunc;
    protected CollisionAlgorithmCreateFunc sphereSphereCF;
    protected CollisionAlgorithmCreateFunc sphereBoxCF;
    protected CollisionAlgorithmCreateFunc boxSphereCF;
    protected CollisionAlgorithmCreateFunc boxBoxCF;
    protected CollisionAlgorithmCreateFunc sphereTriangleCF;
    protected CollisionAlgorithmCreateFunc triangleSphereCF;
    protected CollisionAlgorithmCreateFunc planeConvexCF;
    protected CollisionAlgorithmCreateFunc convexPlaneCF;

    protected CollisionAlgorithmCreateFunc worldCreateFunc;
    protected CollisionAlgorithmCreateFunc swappedWorldCreateFunc;

    public TeraCollisionConfiguration() {
        simplexSolver = new VoronoiSimplexSolver();

        pdSolver = new GjkEpaPenetrationDepthSolver();

        convexConvexCreateFunc = new ConvexConvexAlgorithm.CreateFunc(simplexSolver, pdSolver);
        convexConcaveCreateFunc = new ConvexConcaveCollisionAlgorithm.CreateFunc();
        swappedConvexConcaveCreateFunc = new ConvexConcaveCollisionAlgorithm.SwappedCreateFunc();
        compoundCreateFunc = new CompoundCollisionAlgorithm.CreateFunc();
        swappedCompoundCreateFunc = new CompoundCollisionAlgorithm.SwappedCreateFunc();
        emptyCreateFunc = new EmptyAlgorithm.CreateFunc();

        sphereSphereCF = new SphereSphereCollisionAlgorithm.CreateFunc();
        worldCreateFunc = new WorldCollisionAlgorithm.CreateFunc();
        swappedWorldCreateFunc = new WorldCollisionAlgorithm.SwappedCreateFunc();

        // convex versus plane
        convexPlaneCF = new ConvexPlaneCollisionAlgorithm.CreateFunc();
        planeConvexCF = new ConvexPlaneCollisionAlgorithm.CreateFunc();
        planeConvexCF.swapped = true;
    }

    @Override
    public CollisionAlgorithmCreateFunc getCollisionAlgorithmCreateFunc(BroadphaseNativeType proxyType0, BroadphaseNativeType proxyType1) {
        if ((proxyType0 == SPHERE_SHAPE_PROXYTYPE) && (proxyType1 == SPHERE_SHAPE_PROXYTYPE)) {
            return sphereSphereCF;
        }

        // We're using INVALID_SHAPE_PROTOTYPE for world type
        // TODO: Replace with something better later
        if (proxyType0 == INVALID_SHAPE_PROXYTYPE) {
            return worldCreateFunc;
        } else if (proxyType1 == INVALID_SHAPE_PROXYTYPE) {
            return swappedWorldCreateFunc;
        }

        if (proxyType0.isConvex() && (proxyType1 == STATIC_PLANE_PROXYTYPE))
        {
            return convexPlaneCF;
        }

        if (proxyType1.isConvex() && (proxyType0 == STATIC_PLANE_PROXYTYPE))
        {
            return planeConvexCF;
        }

        if (proxyType0.isConvex() && proxyType1.isConvex()) {
            return convexConvexCreateFunc;
        }

        if (proxyType0.isConvex() && proxyType1.isConcave()) {
            return convexConcaveCreateFunc;
        }

        if (proxyType1.isConvex() && proxyType0.isConcave()) {
            return swappedConvexConcaveCreateFunc;
        }

        if (proxyType0.isCompound()) {
            return compoundCreateFunc;
        }
        else {
            if (proxyType1.isCompound()) {
                return swappedCompoundCreateFunc;
            }
        }

        // failed to find an algorithm
        return emptyCreateFunc;
    }

}