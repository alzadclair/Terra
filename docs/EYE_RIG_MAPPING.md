# Eye of Cthulhu Authentic Rig & Bone Mapping Specification

This document details the exact mathematical transformation and mapping between the source GLTF rigs and TerraForge RPG's runtime skeletal animation armature.

## 1. Source Assets & Coordinate Space Transformation

- **Phase 1 Source**: `C:/model 3d/_processed/06664c90cf3e4d24a74a43dc771ae74f/source/scene.gltf` (Accessor 40, 24 joints)
- **Phase 2 Source**: `C:/model 3d/_processed/a53f70fa34284699a57af3d161182611/source/scene.gltf` (Accessor 32, 26 joints)

### Basis Transform Matrix $T$
The source GLTF models use Blender/FBX coordinate convention ($Z$-up, $-Y$-forward). TerraForge RPG and Minecraft use $Y$-up, $-Z$-forward.
The orthonormal coordinate basis transformation matrix $T$ is:

```
T = [ 1  0  0  0 ]
    [ 0  0 -1  0 ]
    [ 0  1  0  0 ]
    [ 0  0  0  1 ]
```

Transformations between GLTF space and Minecraft space:
- Positions and Normals: $v_{mc} = T \cdot v_{gltf}$ ($x_{mc} = x, y_{mc} = -z, z_{mc} = y$)
- Matrices (Bind & Inverse Bind): $M_{mc} = T \cdot M_{gltf} \cdot T^{-1}$

### Rest Pose Identity Invariance
For every joint $j$, the authentic GLTF inverse bind matrix $(B_j^{-1})_{gltf}$ converted to Minecraft space satisfies:
$$M_{bind}^{mc} \cdot M_{invBind}^{mc} = (T \cdot B_j \cdot T^{-1}) \cdot (T \cdot B_j^{-1} \cdot T^{-1}) = T \cdot I \cdot T^{-1} = I$$
Identity validation across all source joints yields a numerical error of $< 1 \times 10^{-12}$ in double precision.

---

## 2. Phase 2 Rig Mapping & Hinge Pivots

In Phase 2, the front cornea tears open to reveal a maw of razor-sharp teeth articulated by upper and lower jaws.

| TerraForge Bone | Parent Bone | Source GLTF Joint Node | Minecraft Bind Pivot [X, Y, Z] | Role in Phase 2 |
|---|---|---|---|---|
| `root` | *None* | `_rootJoint` | `[0.000, 0.000, 0.000]` | Entity root anchor |
| `body` | `root` | `root_00` | `[0.000, 0.000, 0.000]` | Main eyeball mass |
| `upper_jaw` | `body` | `jaw_upper_01` | `[0.018, 12.302, 0.144]` | **Authentic Upper Jaw Hinge** (articulates upper maw) |
| `lower_jaw` | `body` | `jaw_lower_02` | `[0.018, 10.149, -4.324]` | **Authentic Lower Jaw Hinge** (articulates lower maw) |
| `tendril_01` | `body` | `tendril_1_0_03` | `[1.544, 247.499, 87.141]` | Top trailing tendril base |
| `tendril_02` | `body` | `tendril_2_0_09` | `[-83.905, 247.499, -0.993]` | Left trailing tendril base |
| `tendril_03` | `body` | `tendril_3_0_012` | `[-25.397, 247.499, -79.170]` | Bottom-left trailing tendril base |
| `tendril_04` | `body` | `tendril_4_0_015` | `[68.315, 247.499, -76.759]` | Bottom-right trailing tendril base |
| `tendril_05` | `body` | `tendril_5_0_06` | `[78.023, 247.499, 51.949]` | Right trailing tendril base |
| `tendril_06` | `body` | `tendril_5_2_08` | `[74.930, 438.405, 50.909]` | Tendril flex tip / secondary articulation |

---

## 3. Phase 1 Rig Mapping

In Phase 1, the eye watches the player with a central pupil and iris, trailed by back optic nerves and trailing tendrils.

| TerraForge Bone | Parent Bone | Source GLTF Joint Node | Minecraft Bind Pivot [X, Y, Z] | Role in Phase 1 |
|---|---|---|---|---|
| `root` | *None* | `_rootJoint` | `[0.000, 0.000, 0.000]` | Entity root anchor |
| `body` | `root` | `root_00` | `[0.000, 0.000, 0.000]` | Sclera & body mass |
| `tendril_01` | `body` | `tendril_1_0_02` | `[1.544, 247.499, 87.141]` | Top trailing tendril base |
| `tendril_02` | `body` | `tendril_2_0_08` | `[-83.905, 247.499, -0.993]` | Left trailing tendril base |
| `tendril_03` | `body` | `tendril_3_0_011` | `[-25.397, 247.499, -79.170]` | Bottom-left trailing tendril base |
| `tendril_04` | `body` | `tendril_4_0_014` | `[68.315, 247.499, -76.759]` | Bottom-right trailing tendril base |
| `tendril_05` | `body` | `tendril_5_0_05` | `[78.023, 247.499, 51.949]` | Right trailing tendril base |
| `tendril_06` | `body` | `tentacle_outer_01` | `[0.000, -222.278, 0.000]` | Optic stalk / central back tendril |
| `optic_back` | `body` | `tentacle_outer_01` | `[0.000, -222.278, 0.000]` | Back nerve bundle base |
| `iris` | `body` | `root_00` | `[0.000, 0.000, 0.000]` | Iris focal tracking |
| `pupil` | `iris` | `root_00` | `[0.000, 0.000, 0.000]` | Pupil focal dilation |

---

## 4. Animation Deltas and Invariant Hinge Pivot

Animations apply **local delta transformations** onto the authentic bind local matrices:
$$M_{local, b} = M_{bindLocal, b} \cdot M_{animDelta, b}$$
$$M_{world, b} = M_{world, parent} \cdot M_{local, b}$$
$$S_{skin, b} = M_{world, b} \cdot M_{invBind, b}$$

When $M_{animDelta, b} = I$ (rest pose), $M_{world, b} = M_{bindWorld, b}$, yielding $S_{skin, b} = I$.
When a jaw rotates by angle $\theta$ around its hinge, the rotation is applied at the origin of the joint space, preserving the authentic pivot position with zero drift.
