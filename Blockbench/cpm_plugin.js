(function() {
	var import_action, export_action;
	Plugin.register('cpm_plugin', {
        name: 'Customizable Player Models Plugin',
        author: 'tom5454',
        description: 'Customizable Player Models Project (.cpmproject) support for Blockbench.',
        icon: 'icon-player',
        version: '0.0.3',
        variant: 'both',
        onload() {
			var VANILLA_VALUES = {
				head: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: -8.0, z: -4.0 },
					size: { x: 8.0, y: 8.0, z: 8.0 },
					uv: { u: 0, v: 0 },
				},
				body: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: 0.0, z: -2.0 },
					size: { x: 8.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 16 },
				},
				left_arm: {
					pos: { x: 5.0, y: 2.0, z: 0.0 },
					offset: { x: -1.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 32, v: 48 },
				},
				right_arm: {
					pos: { x: -5.0, y: 2.0, z: 0.0 },
					offset: { x: -3.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 40, v: 16 },
				},
				left_arm: {
					pos: { x: 5.0, y: 2.0, z: 0.0 },
					offset: { x: -1.0, y: -2.0, z: -2.0 },
					size: { x: 3.0, y: 12.0, z: 4.0 },
					uv: { u: 32, v: 48 },
				},
				right_arm: {
					pos: { x: -5.0, y: 2.0, z: 0.0 },
					offset: { x: -2.0, y: -2.0, z: -2.0 },
					size: { x: 3.0, y: 12.0, z: 4.0 },
					uv: { u: 40, v: 16 },
				},
				left_leg: {
					pos: { x: 1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 48 },
				},
				right_leg: {
					pos: { x: -1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
				},
				cape: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -5.0, y: 0.0, z: -1.0 },
					size: { x: 10.0, y: 16.0, z: 1.0 },
					uv: { u: 0, v: 0 },
					custom: true,
				},
				elytra_left: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -10.0, y: 0.0, z: 0.0 },
					size: { x: 10.0, y: 20.0, z: 2.0 },
					uv: { u: 22, v: 0 },
					custom: true,
				},
				elytra_right: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: 0.0, y: 0.0, z: 0.0 },
					size: { x: 10.0, y: 20.0, z: 2.0 },
					uv: { u: 22, v: 0 },
					custom: true,
				},
				armor_helmet: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: -8.0, z: -4.0 },
					size: { x: 8.0, y: 8.0, z: 8.0 },
					uv: { u: 0, v: 0 },
					custom: true,
				},
				armor_body: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: 0.0, z: -2.0 },
					size: { x: 8.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 16 },
					custom: true,
				},
				armor_left_arm: {
					pos: { x: 5.0, y: 2.0, z: 0.0 },
					offset: { x: -1.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 40, v: 16 },
					custom: true,
				},
				armor_right_arm: {
					pos: { x: -5.0, y: 2.0, z: 0.0 },
					offset: { x: -3.0, y: -2.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 40, v: 16 },
					custom: true,
				},
				armor_leggings_body: {
					pos: { x: 0.0, y: 0.0, z: 0.0 },
					offset: { x: -4.0, y: 0.0, z: -2.0 },
					size: { x: 8.0, y: 12.0, z: 4.0 },
					uv: { u: 16, v: 16 },
					custom: true,
				},
				armor_left_leg: {
					pos: { x: 1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
				},
				armor_right_leg: {
					pos: { x: -1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
				},
				armor_left_foot: {
					pos: { x: 1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
				},
				armor_right_foot: {
					pos: { x: -1.9, y: 12.0, z: 0.0 },
					offset: { x: -2.0, y: 0.0, z: -2.0 },
					size: { x: 4.0, y: 12.0, z: 4.0 },
					uv: { u: 0, v: 16 },
					custom: true,
				},
			};
			var NAME_MAP = {
				Head: "head",
				Body: "body",
				RightArm: "right_arm",
				LeftArm: "left_arm",
				RightLeg: "right_leg",
				LeftLeg: "left_leg"
			};
			var ROOT_NAMES = [
				"Head",
				"Body",
				"Right Arm",
				"Left Arm",
				"Right Leg",
				"Left Leg"
			];

        	var codec = new Codec('cpmproject', {
        		name: 'Customizable Player Models Project',
    			extension: 'cpmproject',
    			remember: false,
    			compile(options) {
					let all_groups = getAllGroups();
					let loose_cubes = [];
					let box_uv = Project.box_uv;
					Cube.all.forEach(cube => {
						if (cube.parent == 'root') loose_cubes.push(cube)
					})
					if (loose_cubes.length) {
						let group = new Group({
							name: 'bb_main'
						});
						group.is_catch_bone = true;
						group.createUniqueName()
						all_groups.push(group)
						group.children.replace(loose_cubes)
					}
					
					all_groups.slice().forEach(group => {
						let subgroups = [];
						let group_i = all_groups.indexOf(group);
						group.children.forEachReverse(cube => {
							if (cube instanceof Cube == false || !cube.export) return;
							if (!cube.rotation.allEqual(0)) {
								let sub = subgroups.find(s => {
									if (!s.rotation.equals(cube.rotation)) return false;
									if (s.rotation.filter(n => n).length > 1) {
										return s.origin.equals(cube.origin)
									} else {
										for (var i = 0; i < 3; i++) {
											if (s.rotation[i] == 0 && s.origin[i] != cube.origin[i]) return false;
										}
										return true;
									}
								})
								if (!sub) {
									sub = new Group({
										rotation: cube.rotation,
										origin: cube.origin,
										name: `${cube.name}_r1`
									})
									sub.parent = group;
									sub.is_rotation_subgroup = true;
									sub.createUniqueName(all_groups)
									subgroups.push(sub)
									group_i++;
									all_groups.splice(group_i, 0, sub);
								}
								sub.children.push(cube);
							}
						});
					});
					let p = {};
					p.version = 1;
					p.skinSize = {};
					p.skinSize.x = Project.texture_width;
					p.skinSize.y = Project.texture_height;
					p.elements = [];
					let modelTree = {};
					function convert(elem, cube) {
						if(box_uv) {
							elem.u = cube.uv_offset[0];
							elem.v = cube.uv_offset[1];
							elem.mirror = cube.mirror_uv;
						} else {
							elem.faceUV = {};
							for (var faceName of Object.keys(cube.faces)) {
								let face = cube.faces[faceName];
								
								if(faceName == "east")faceName = "west";
								else if(faceName == "west")faceName = "east";
								
								if(face.uv_size[0] != 0 && face.uv_size[1] != 0) {
									let f = {};
									elem.faceUV[faceName] = f;
									f.sx = face.uv[0];
									f.sy = face.uv[1];
									f.ex = face.uv[2];
									f.ey = face.uv[3];
									if(faceName == "up" || faceName == "down")
										f.rot = ((face.rotation + 180) % 360).toString();
									else
										f.rot = face.rotation.toString();
									f.autoUV = cube.autouv ? true : false;
								}
							}
							elem.u = 0;
							elem.v = 0;
							elem.mirror = false;
						}
						elem.mcScale = cube.inflate;
						elem.offset = {x: group.origin[0] - cube.to[0], y: -cube.from[1] - cube.size(1) + group.origin[1], z: cube.from[2] - group.origin[2]};
						elem.size = {x: cube.size(0, true), y: cube.size(1, true), z: cube.size(2, true)};
					}
					for (var group of all_groups) {
						if ((group instanceof Group === false && !group.is_catch_bone) || !group.export) continue;
						if (group.parent instanceof Group) continue;
						let part = {};
						part.id = NAME_MAP[group.name] || group.name;
						p.elements.push(part);
						part.show = true;
						var origin = group.origin.slice();
						origin[0] *= -1;
						origin[1] *= -1;
						origin[1] += 24;
						if(VANILLA_VALUES[part.id]) {
							var val = VANILLA_VALUES[part.id];
							origin[0] -= val.pos.x;
							origin[1] -= val.pos.y;
							origin[2] -= val.pos.z;
						}
						part.pos = {x: origin[0], y: origin[1], z: origin[2]};
						part.rotation = {x: 0, y: 0, z: 0};
						part.children = [];
						modelTree[group.uuid] = part.children;
						for (var cube of group.children) {
							if (cube instanceof Cube === false || !cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup)) continue;
							if (cube.name == "cube" || ROOT_NAMES.includes(cube.name) || group.name == cube.name) {
								part.show = cube.visibility;
								continue;
							}
							let elem = {};
							part.children.push(elem);
							elem.name = cube.name;
							elem.rotation = {x: 0, y: 0, z: 0};
							elem.textureSize = 1;
							elem.texture = true;
							elem.show = true;
							convert(elem, cube);
							elem.pos = {x: 0, y: 0, z: 0};
							elem.color = "0";
						}
					}
					for (var group of all_groups) {
						if ((group instanceof Group === false && !group.is_catch_bone) || !group.export) continue;
						if (!(group.parent instanceof Group)) continue;
						let elem = {};
						elem.name = group.name;
						elem.rotation = {x: -group.rotation[0], y: -group.rotation[1], z: group.rotation[2]};
						var origin = group.origin.slice();
						if (group.parent instanceof Group) {
							origin.V3_subtract(group.parent.origin)
						}
						origin[0] *= -1;
						origin[1] *= -1;
						if (group.parent instanceof Group === false) {
							origin[1] += 24
						}
						elem.pos = {x: origin[0], y: origin[1], z: origin[2]};
						elem.textureSize = 1;
						elem.texture = true;
						elem.color = "0";
						let e;
						modelTree[group.uuid] = [];
						for (var cube of group.children) {
							if (cube instanceof Cube === false || !cube.export || (!cube.rotation.allEqual(0) && !group.is_rotation_subgroup)) continue;
							e = jQuery.extend(true, {}, elem);
							convert(e, cube);
							e.show = cube.visibility;
							e.name = cube.name;
							modelTree[group.parent.uuid].push(e);
						}
						if(!e) {
							e = jQuery.extend(true, {}, elem);
							e.u = 0;
							e.v = 0;
							e.mcScale = 0;
							e.mirror = false;
							e.show = true;
							e.size = {x: 0, y: 0, z: 0};
							modelTree[group.parent.uuid].push(e);
						}
						e.children = modelTree[group.uuid];
					}
					let zip = new JSZip();
					zip.file("config.json", JSON.stringify(p));
					var tex = Texture.all[0];
					console.log(tex.source);
					var idx = tex.source.indexOf('base64,') + 'base64,'.length;
					var content = tex.source.substring(idx);
					zip.file("skin.png", content, {base64: true});
					let event = {model: zip.generateAsync({
    					type: "blob",
    					compression: "DEFLATE",
    					compressionOptions: {
        					level: 9
    					}
					}), options};
					this.dispatchEvent('compile', event);
					return event.model;
    			},
				write(content, path) {
					var scope = codec;
					if (fs.existsSync(path) && this.overwrite) {
						this.overwrite(content, path, path => scope.afterSave(path))
					} else {
						Blockbench.writeFile(path, {content, savetype: "zip"}, path => scope.afterSave(path));
					}
				},
				export() {
					console.log("Export");
					var scope = codec;
					scope.compile().then(content => {
						Blockbench.export({
							resource_id: 'model',
							type: scope.name,
							extensions: [scope.extension],
							name: scope.fileName(),
							startpath: scope.startPath(),
							content,
							custom_writer: isApp ? (a, b) => scope.write(a, b) : null,
						}, path => scope.afterDownload(path));
					});
				},
    			parse(arraybuffer) {
					this.dispatchEvent('parse', {arraybuffer});
    				newProject(Formats.modded_entity);
    				var loadedZip = new JSZip().loadAsync(arraybuffer);
    				loadedZip.then(zip => {
    					zip.file("config.json").async("string").
    					then(json => {
    						var p = JSON.parse(json);
    						Project.texture_width = p.skinSize.x;
    						Project.texture_height = p.skinSize.y;
    						Project.box_uv = true;
		
							function ceil(val) {
								return Math.ceil(val - 0.01);
							}
							
							function floor(val) {
								return Math.floor(val + 0.01);
							}
		
							function boxToPFUV(cube, texSc = 1, scX = 1, scY = 1, scZ = 1) {
								var dx = ceil(cube.to[0] - cube.from[0]) * texSc / (!scX ? 1 : scX);
								var dy = ceil(cube.to[1] - cube.from[1]) * texSc / (!scY ? 1 : scY);
								var dz = ceil(cube.to[2] - cube.from[2]) * texSc / (!scZ ? 1 : scZ);
								var u = floor(cube.uv_offset[0] * texSc);
								var v = floor(cube.uv_offset[1] * texSc);
								var f4 = u;
								var f5 = u + dz;
								var f6 = u + dz + dx;
								var f7 = u + dz + dx + dx;
								var f8 = u + dz + dx + dz;
								var f9 = u + dz + dx + dz + dx;
								var f10 = v;
								var f11 = v + dz;
								var f12 = v + dz + dy;
								cube.faces.up.uv = [f6, f11, f5, f10];
								cube.faces.down.uv = [f7, f10, f6, f11];
								cube.faces.east.uv = [f4, f11, f5, f12];
								cube.faces.north.uv = [f5, f11, f6, f12];
								cube.faces.west.uv = [f6, f11, f8, f12];
								cube.faces.south.uv = [f8, f11, f9, f12];
								if (cube.mirror_uv) {
									for (var faceName of Object.keys(cube.faces)) {
										let face = cube.faces[faceName];
										var t = face.uv[0];
										face.uv[0] = face.uv[2];
										face.uv[2] = t;
									}
									var uv = cube.faces.east.uv;
									cube.faces.east.uv = cube.faces.west.uv;
									cube.faces.west.uv = uv;
								}
								cube.autouv = 0;
							}
    						
    						function loadChildren(data, parent) {
    							for(var i = 0;i<data.length;i++) {
    								var e = data[i];
    								var gr = new Group({
    									name: e.name,
	    								origin: [0, 24, 0],
    								}).init();
									gr.extend({origin: [-e.pos.x, 24-e.pos.y, e.pos.z]});
									gr.origin.V3_add(parent.origin)
									gr.origin[1] -= 24;
									gr.addTo(parent);
									gr.extend({
										rotation: [
											-e.rotation.x,
											-e.rotation.y,
											e.rotation.z,
										]
									});
									gr.visibility = e.show;
	    							var cube = new Cube({
	    								name: e.name,
										from: [
											gr.origin[0] - e.offset.x - e.size.x * e.scale.x,
											gr.origin[1] - e.offset.y - e.size.y * e.scale.y,
											gr.origin[2] + e.offset.z
										],
										inflate: e.mcScale,
										mirror_uv: e.mirror,
										uv_offset: [e.u, e.v]
									});
									cube.extend({
										to: [
											cube.from[0] + floor(e.size.x * e.scale.x),
											cube.from[1] + floor(e.size.y * e.scale.y),
											cube.from[2] + floor(e.size.z * e.scale.z),
										]
									});
									if(e.faceUV || e.textureSize > 1 || e.singleTex || e.scale.x != 1 || e.scale.y != 1 || e.scale.z != 1) {
										if(Project.box_uv) {
											Project.box_uv = false;
											Cube.all.forEach(c => boxToPFUV(c));
										}
										cube.autouv = 0;
										if(e.faceUV) {
											for (var faceName of Object.keys(cube.faces)) {
												let face = cube.faces[faceName];
												
												if(faceName == "east")faceName = "west";
												else if(faceName == "west")faceName = "east";
												
												if(e.faceUV[faceName]) {
													var ef = e.faceUV[faceName];
													face.uv = [ef.sx, ef.sy, ef.ex, ef.ey];
													if(faceName == "up" || faceName == "down")
														face.rotation = ((ef.rot + 180) % 360);
													else
														face.rotation = ef.rot;
												} else {
													face.uv = [0, 0, 0, 0];
												}
											}
										} else if(e.singleTex) {
											if(e.mcScale == 0 && (e.pos.x == 0 || e.pos.y == 0 || e.pos.z == 0)) {
												var texU = e.u * e.textureSize;
												var texV = e.v * e.textureSize;
												if(e.pos.x == 0) {
													var tu = texU + ceil(e.size.z * e.textureSize);
													var tv = texV + ceil(e.size.y * e.textureSize);
													for (var faceName of Object.keys(cube.faces)) {
														let face = cube.faces[faceName];
														if(faceName == "west") {
															face.uv = [texU, texV, tu, tv];
														} else if(faceName == "east") {
															face.uv = [tu, texV, texU, tv];
														} else {
															face.uv = [0, 0, 0, 0];
														}
													}
												} else if(e.pos.y == 0) {
													var tu = texU + ceil(e.size.x * e.textureSize);
													var tv = texV + ceil(e.size.z * e.textureSize);
													for (var faceName of Object.keys(cube.faces)) {
														let face = cube.faces[faceName];
														if(faceName == "up") {
															face.uv = [tu, texV, texU, tv];
														} else if(faceName == "down") {
															face.uv = [texU, texV, tu, tv];
														} else {
															face.uv = [0, 0, 0, 0];
														}
													}
												} else if(e.pos.z == 0) {
													var tu = texU + ceil(e.size.x * e.textureSize);
													var tv = texV + ceil(e.size.y * e.textureSize);
													for (var faceName of Object.keys(cube.faces)) {
														let face = cube.faces[faceName];
														if(faceName == "north") {
															face.uv = [texU, texV, tu, tv];
														} else if(faceName == "south") {
															face.uv = [tu, texV, texU, tv];
														} else {
															face.uv = [0, 0, 0, 0];
														}
													}
												}
											} else {
												var size = Math.max(e.pos.x, e.pos.y, e.pos.z);
												for (var faceName of Object.keys(cube.faces)) {
													let face = cube.faces[faceName];
													face.uv[0] = e.u * e.textureSize;
													face.uv[1] = e.v * e.textureSize;
													face.uv[2] = (e.u + size) * e.textureSize;
													face.uv[3] = (e.v + size) * e.textureSize;
												}
											}
											if(e.mirror) {
												for (var faceName of Object.keys(cube.faces)) {
													let face = cube.faces[faceName];
													var t = face.uv[0];
													face.uv[0] = face.uv[2];
													face.uv[2] = t;
												}
												var uv = cube.faces.east.uv;
												cube.faces.east.uv = cube.faces.west.uv;
												cube.faces.west.uv = uv;
											}
										} else {
											boxToPFUV(cube, e.textureSize, e.scale.x, e.scale.y, e.scale.z);
										}
									} else if(!Project.box_uv) {
										boxToPFUV(cube);
									}
									cube.addTo(gr).init();
									if (e.children) {
	    								loadChildren(e.children, gr);
	    							}
    							}
    						}
    						
    						for(var i = 0;i<p.elements.length;i++) {
    							var data = p.elements[i];
    							var vals = VANILLA_VALUES[data.id];
    							if(vals) {
    								var gr = new Group({
    									name: data.id, 
    									origin: [0, 24, 0]
    								}).init();
									gr.extend({origin: [-vals.pos.x, 24-vals.pos.y, vals.pos.z]});
    								var cube = new Cube({
	    								name: data.id,
	    								origin: [0, 0, 0],
	    								from: [
											gr.origin[0] - vals.offset.x - vals.size.x,
											gr.origin[1] - vals.offset.y - vals.size.y,
											gr.origin[2] + vals.offset.z
										],
	    								rotation: [0, 0, 0],
	    								uv_offset: [vals.uv.u, vals.uv.v]
	    							});
									cube.extend({
										to: [
											cube.from[0] + floor(vals.size.x),
											cube.from[1] + floor(vals.size.y),
											cube.from[2] + floor(vals.size.z),
										]
									});
									cube.visibility = data.show;
    								cube.addTo(gr).init();
									if(!Project.box_uv) {
										boxToPFUV(cube);
									}
									
    								if(data.children) {
    									loadChildren(data.children, gr);
    								}
    							}
    						}
    						
    						this.dispatchEvent('parsed', {arraybuffer});
							Canvas.updateAllBones();
    					});
    					zip.file("skin.png").async("base64").then(img => {
							var texture = new Texture().fromDataURL('data:image/png;base64,' + img);
							texture.name = "skin";
							texture.add();
						});
    				});
    			}
        	});
        	import_action = new Action('import_cpmproject', {
    			name: 'Import Customizable Player Models Project',
    			description: '',
    			icon: 'icon-player',
    			category: 'file',
    			click() {
    				Blockbench.import({
    					extensions: ['cpmproject'],
    					type: 'Customizable Player Models Project',
    					readtype: 'binary',
    					resource_id: 'cpmproject_files'
    				}, files => {
    					codec.parse(files[0].content);
    					csname = files[0].name.replace(".cpmproject", "").replace(/\s+/g, "_").toLowerCase();
    					Project.name = csname;
    					Project.geometry_name = csname;
    				})
    			}
    		})
    		export_action = new Action('export_cpmproject', {
    			name: 'Export Customizable Player Models Project',
    			description: '',
    			icon: 'icon-player',
    			category: 'file',
    			click() {
    				codec.export();
    			}
    		})

    		MenuBar.addAction(import_action, 'file.import')
    		MenuBar.addAction(export_action, 'file.export')
        },
        onunload() {
        	import_action.delete();
    		export_action.delete();
        }
    });
})();