"use client";

import { useEffect, useRef } from "react";
import * as THREE from "three";
import { GLTFLoader } from "three/addons/loaders/GLTFLoader.js";

function disposeModel(model: THREE.Object3D) {
  model.traverse((object) => {
    if (!(object instanceof THREE.Mesh)) {
      return;
    }

    object.geometry.dispose();
    const materials = Array.isArray(object.material)
      ? object.material
      : [object.material];

    materials.forEach((material) => {
      Object.values(material).forEach((value) => {
        if (value instanceof THREE.Texture) {
          value.dispose();
        }
      });
      material.dispose();
    });
  });
}

export default function HeroPcModel() {
  const viewportRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const viewport = viewportRef.current;
    if (!viewport) {
      return;
    }

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(34, 1, 0.01, 100);
    const hemisphereLight = new THREE.HemisphereLight(0xe8f2ff, 0x202b3a, 2);
    const keyLight = new THREE.DirectionalLight(0xffffff, 2.8);
    const fillLight = new THREE.DirectionalLight(0x8abfff, 1.5);

    keyLight.castShadow = true;
    keyLight.shadow.mapSize.set(1024, 1024);
    keyLight.shadow.bias = -0.0001;
    keyLight.shadow.normalBias = 0.02;
    keyLight.shadow.radius = 4;
    scene.add(hemisphereLight, keyLight, keyLight.target, fillLight);

    let renderer: THREE.WebGLRenderer;
    try {
      renderer = new THREE.WebGLRenderer({
        alpha: true,
        antialias: true,
        powerPreference: "low-power",
      });
    } catch {
      return;
    }

    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.75));
    renderer.setClearColor(0x000000, 0);
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 1.1;
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    renderer.domElement.className = "absolute inset-0 h-full w-full";
    viewport.appendChild(renderer.domElement);

    let isDisposed = false;
    let model: THREE.Object3D | null = null;
    let shadowPlane: THREE.Mesh<THREE.PlaneGeometry, THREE.ShadowMaterial> | null = null;
    let modelRadius = 0;
    let scrollFrame = 0;
    const viewDirection = new THREE.Vector3(1.4, 1.2, 2.1).normalize();
    const cameraRight = new THREE.Vector3().crossVectors(
      new THREE.Vector3(0, 1, 0),
      viewDirection,
    ).normalize();

    const draw = () => {
      if (!isDisposed) {
        renderer.render(scene, camera);
      }
    };

    const render = () => {
      if (isDisposed) {
        return;
      }

      const { width, height } = viewport.getBoundingClientRect();
      if (!width || !height) {
        return;
      }

      renderer.setSize(width, height, false);
      camera.aspect = width / height;
      if (modelRadius) {
        const verticalFov = THREE.MathUtils.degToRad(camera.fov);
        const compactLayout = camera.aspect < 1.7;
        const horizontalFov = 2 * Math.atan(
          Math.tan(verticalFov / 2) * camera.aspect,
        );
        const horizontalDistanceMultiplier = compactLayout ? 1.2 : 1.05;
        const distance = Math.max(
          modelRadius / Math.sin(verticalFov / 2) * (compactLayout ? 1.35 : 1.05),
          modelRadius / Math.sin(horizontalFov / 2) * horizontalDistanceMultiplier,
        );
        const horizontalOffset = modelRadius * THREE.MathUtils.clamp(
          camera.aspect - 0.75,
          0,
          1.6,
        );
        const target = cameraRight.clone().multiplyScalar(-horizontalOffset);
        const maxVerticalOffset = Math.max(
          0,
          distance * Math.tan(verticalFov / 2) - modelRadius * 1.08,
        );
        target.y = compactLayout
          ? Math.min(modelRadius * 0.75, maxVerticalOffset)
          : 0;

        camera.position.copy(target).addScaledVector(viewDirection, distance);
        camera.near = Math.max(modelRadius / 1000, 0.01);
        camera.far = modelRadius * 20;
        camera.lookAt(target);
      }
      camera.updateProjectionMatrix();
      draw();
    };

    const handleScroll = () => {
      if (scrollFrame) {
        return;
      }

      scrollFrame = window.requestAnimationFrame(() => {
        scrollFrame = 0;
        if (model && !isDisposed) {
          model.rotation.y = window.scrollY * 0.0025;
          draw();
        }
      });
    };

    window.addEventListener("scroll", handleScroll, { passive: true });
    const resizeObserver = new ResizeObserver(render);
    resizeObserver.observe(viewport);

    const loader = new GLTFLoader();
    loader.load(
      "/models/homepage/pc.glb",
      (gltf) => {
        if (isDisposed) {
          disposeModel(gltf.scene);
          return;
        }

        model = gltf.scene;
        const bounds = new THREE.Box3().setFromObject(model);
        const center = bounds.getCenter(new THREE.Vector3());
        const dimensions = bounds.getSize(new THREE.Vector3());
        const sphere = bounds.getBoundingSphere(new THREE.Sphere());

        const modelScale = 1.3;
        model.scale.setScalar(modelScale);
        model.position.sub(center.clone().multiplyScalar(modelScale));
        model.traverse((object) => {
          if (object instanceof THREE.Mesh) {
            object.castShadow = true;
            object.receiveShadow = true;
          }
        });
        scene.add(model);

        model.rotation.y = window.scrollY * 0.0025;
        modelRadius = sphere.radius;
        keyLight.position.copy(viewDirection).multiplyScalar(modelRadius * 1.2);
        keyLight.position.addScaledVector(cameraRight, -modelRadius * 2.5);
        keyLight.position.y += modelRadius * 3.5;
        fillLight.position.copy(cameraRight).multiplyScalar(modelRadius * 2.4);
        fillLight.position.addScaledVector(viewDirection, modelRadius * 0.8);
        fillLight.position.y += modelRadius * 1.5;

        const shadowExtent = modelRadius * modelScale * 2.5;
        keyLight.shadow.camera.left = -shadowExtent;
        keyLight.shadow.camera.right = shadowExtent;
        keyLight.shadow.camera.top = shadowExtent;
        keyLight.shadow.camera.bottom = -shadowExtent;
        keyLight.shadow.camera.near = 0.1;
        keyLight.shadow.camera.far = modelRadius * 10;
        keyLight.shadow.camera.updateProjectionMatrix();

        const shadowMaterial = new THREE.ShadowMaterial({
          color: 0x07101c,
          opacity: 0.3,
        });
        shadowPlane = new THREE.Mesh(
          new THREE.PlaneGeometry(
            modelRadius * modelScale * 8,
            modelRadius * modelScale * 8,
          ),
          shadowMaterial,
        );
        shadowPlane.rotation.x = -Math.PI / 2;
        shadowPlane.position.y =
          -(dimensions.y * modelScale) / 2 - modelRadius * modelScale * 0.02;
        shadowPlane.receiveShadow = true;
        scene.add(shadowPlane);

        render();
      },
      undefined,
      (error) => {
        console.error("Failed to load the homepage PC model.", error);
      },
    );

    return () => {
      isDisposed = true;
      window.removeEventListener("scroll", handleScroll);
      if (scrollFrame) {
        window.cancelAnimationFrame(scrollFrame);
      }
      resizeObserver.disconnect();
      if (model) {
        disposeModel(model);
      }
      if (shadowPlane) {
        shadowPlane.geometry.dispose();
        shadowPlane.material.dispose();
      }
      renderer.dispose();
      renderer.domElement.remove();
    };
  }, []);

  return (
    <div
      ref={viewportRef}
      aria-hidden="true"
      className="
        pointer-events-none absolute inset-0 z-0 overflow-hidden
        bg-[linear-gradient(90deg,_#ffffff_0%,_#ffffff_28%,_#b9bec6_50%,_#596575_72%,_#0b1019_100%)]
      "
    />
  );
}
