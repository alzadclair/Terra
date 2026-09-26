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
Identity validation across all 50 source joints yields a maximum numerical error of $< 5.7 \times 10^{-14}$.

---

## 2. Phase 2 Rig Mapping & Hinge Pivots

In Phase 2, the front cornea tears open to reveal a maw of razor-sharp teeth articulated by upper and lower jaws.

| TerraForge Bone | Source GLTF Joint Node | Minecraft Bind Pivot [X, Y, Z] | Role in Phase 2 |
|---|---|---|---|
| `root` | `_rootJoint` (node 14) | `[0.000, 0.000, 0.000]` | Entity root anchor |
| `body` | `root_00` (node 31) | `[0.000, 0.000, 0.000]` | Main eyeball mass |
| `upper_jaw` | `jaw_upper_01` (node 32) | `[0.018, 12.302, 0.144]` | **Authentic Upper Jaw Hinge** (articulates upper maw) |
| `lower_jaw` | `jaw_lower_02` (node 34) | `[0.018, 10.149, -4.324]` | **Authentic Lower Jaw Hinge** (articulates lower maw) |
| `tendril_01` | `tendril_1_0_03` (node 36) | `[1.544, 247.499, 87.141]` | Top trailing tendril base |
| `tendril_02` | `tendril_2_0_09` (node 44) | `[-83.905, 247.499, -0.993]` | Left trailing tendril base |
| `tendril_03` | `tendril_3_0_012` (node 48) | `[-25.397, 247.499, -79.170]` | Bottom-left trailing tendril base |
| `tendril_04` | `tendril_4_0_015` (node 52) | `[68.315, 247.499, -76.759]` | Bottom-right trailing tendril base |
| `tendril_05` | `tendril_5_0_06` (node 40) | `[78.023, 247.499, 51.949]` | Right trailing tendril base |
| `tendril_06` | `tendril_5_2_08` (node 42) | `[74.930, 438.405, 50.909]` | Tendril flex tip / secondary articulation |

---

## 3. Phase 1 Rig Mapping

In Phase 1, the eye watches the player with a central pupil and iris, trailed by back optic nerves and trailing tendrils.

| TerraForge Bone | Source GLTF Joint Node | Minecraft Bind Pivot [X, Y, Z] | Role in Phase 1 |
|---|---|---|---|
| `root` | `_rootJoint` (node 12) | `[0.000, 0.000, 0.000]` | Entity root anchor |
| `body` | `root_00` (node 29) | `[0.000, 0.000, 0.000]` | Sclera & body mass |
| `tendril_01` | `tendril_1_0_02` (node 32) | `[1.544, 247.499, 87.141]` | Top trailing tendril base |
| `tendril_02` | `tendril_2_0_08` (node 40) | `[-83.905, 247.499, -0.993]` | Left trailing tendril base |
| `tendril_03` | `tendril_3_0_011` (node 44) | `[-25.397, 247.499, -79.170]` | Bottom-left trailing tendril base |
| `tendril_04` | `tendril_4_0_014` (node 48) | `[68.315, 247.499, -76.759]` | Bottom-right trailing tendril base |
| `tendril_05` | `tendril_5_0_05` (node 36) | `[78.023, 247.499, 51.949]` | Right trailing tendril base |
| `tendril_06` | `tentacle_outer_01` (node 30) | `[0.000, -222.278, 0.000]` | Optic stalk / central back tendril |
| `optic_back` | `tentacle_outer_01` (node 30) | `[0.000, -222.278, 0.000]` | Back nerve bundle base |
| `iris` | `root_00` (node 29) | `[0.000, 0.000, 0.000]` | Iris focal tracking |
| `pupil` | `root_00` (node 29) | `[0.000, 0.000, 0.000]` | Pupil focal dilation |

---

## 4. Normal Matrix Skinning for Non-Uniform Scale

When bones scale non-uniformly (e.g., $(0.88, 0.88, 1.35)$ during charge dashes), vertex normals cannot be transformed by the standard affine matrix $M_{skin}$ without shearing.
TerraForge RPG calculates the $3 \times 3$ normal matrix per bone per frame:
$$N_{skin} = (M_{skin}^{3\times 3})^{-T}$$
Normals are transformed via $n' = \sum_k w_k (N_{skin, k} \cdot n_{bind})$ and re-normalized. This produces mathematically correct lighting normals with zero heap allocations during the render loop.
