package cascade.features.modules.combat;

import phobos.event.events.Render3DEvent;
import phobos.event.events.PacketEvent.Receive;
import phobos.event.events.PacketEvent.Send;
import phobos.features.modules.Module;
import phobos.features.modules.Module.Category;
import phobos.features.setting.ParentSetting;
import phobos.features.setting.Setting;
import phobos.util.BlockUtil;
import phobos.util.EntityUtil;
import phobos.util.MathUtil;
import phobos.util.RenderUtil;
import phobos.util.Timer;
import phobos.util.EntityUtil.SwingType;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoCrystalRewrite extends Module {
   public ParentSetting targetParent = this.registerParent(new ParentSetting("Targets"));
   public Setting<Float> targetRange;
   public ParentSetting placingParent;
   public Setting<Float> placeRange;
   public Setting<Float> placeWallRange;
   public Setting<Integer> placeDelay;
   public Setting<Float> placeMinimumDamage;
   public Setting<Float> placeMaximumSelfDamage;
   public Setting<Boolean> placeAntiSuicide;
   public Setting<Boolean> placePacket;
   public Setting<Boolean> placeSwing;
   public Setting<SwingType> placeSwingHand;
   public ParentSetting explodingParent;
   public Setting<Float> explodeRange;
   public Setting<Float> explodeWallRange;
   public Setting<Integer> breakDelay;
   public Setting<Float> explodeMinimumDamage;
   public Setting<Float> explodeMaximumSelfDamage;
   public Setting<Boolean> explodeAntiSuicide;
   public Setting<Boolean> explodePacket;
   public Setting<Boolean> explodeSwing;
   public Setting<SwingType> explodeSwingHand;
   public ParentSetting facePlacingParent;
   public Setting<Float> facePlaceHp;
   public ParentSetting predictingParent;
   public Setting<Boolean> predict;
   public Setting<Integer> predictDelay;
   public Setting<Boolean> predictSetDead;
   public ParentSetting rendering;
   public Setting<AutoCrystalRewrite.RenderType> renderType;
   public Setting<Boolean> placeBox;
   public Setting<Color> placeBoxColor;
   public Setting<Boolean> placeOutline;
   public Setting<Color> placeOutlineColor;
   public Setting<Float> placeLineWidth;
   public Setting<Boolean> placeText;
   public Setting<Boolean> explodeBox;
   public Setting<Color> explodeBoxColor;
   public Setting<Boolean> explodeOutline;
   public Setting<Color> explodeOutlineColor;
   public Setting<Float> explodeLineWidth;
   public Setting<Boolean> explodeText;
   public AutoCrystalRewrite.PlacePosition placePosition;
   public AutoCrystalRewrite.ExplodePosition explodePosition;
   public EntityPlayer target;
   public Timer placeTimer;
   public Timer explodeTimer;
   public Timer predictTimer;
   public static AutoCrystalRewrite INSTANCE = new AutoCrystalRewrite();
   public AutoCrystalRewrite.CurrentThread currentThread;

   public AutoCrystalRewrite() {
      super("AutoCrystalRewrite", Category.COMBAT, "rewrite by le god zprestige_");
      this.targetRange = this.register((new Setting("Target Range", 10.0F, 0.0F, 15.0F)).setParent(this.targetParent));
      this.placingParent = this.registerParent(new ParentSetting("Placing"));
      this.placeRange = this.register((new Setting("Place Range", 5.0F, 0.0F, 6.0F)).setParent(this.placingParent));
      this.placeWallRange = this.register((new Setting("Place Wall Range", 5.0F, 0.0F, 6.0F)).setParent(this.placingParent));
      this.placeDelay = this.register((new Setting("Place Delay", 10, 0, 500)).setParent(this.placingParent));
      this.placeMinimumDamage = this.register((new Setting("Place Minimum Damage", 8.0F, 0.0F, 36.0F)).setParent(this.placingParent));
      this.placeMaximumSelfDamage = this.register((new Setting("Place Maximum Self Damage", 8.0F, 0.0F, 36.0F)).setParent(this.placingParent));
      this.placeAntiSuicide = this.register((new Setting("Place Anti Suicide", false)).setParent(this.placingParent));
      this.placePacket = this.register((new Setting("Place Packet", false)).setParent(this.placingParent));
      this.placeSwing = this.register((new Setting("Place Swing", false)).setParent(this.placingParent));
      this.placeSwingHand = this.register((new Setting("Place Swing Offhand", SwingType.MainHand, (v) -> {
         return (Boolean)this.placeSwing.getValue();
      })).setParent(this.placingParent));
      this.explodingParent = this.registerParent(new ParentSetting("Exploding"));
      this.explodeRange = this.register((new Setting("Explode Range", 5.0F, 0.0F, 6.0F)).setParent(this.explodingParent));
      this.explodeWallRange = this.register((new Setting("Explode Wall Range", 5.0F, 0.0F, 6.0F)).setParent(this.explodingParent));
      this.breakDelay = this.register((new Setting("Break Delay", 60, 0, 500)).setParent(this.explodingParent));
      this.explodeMinimumDamage = this.register((new Setting("Explode Minimum Damage", 8.0F, 0.0F, 36.0F)).setParent(this.explodingParent));
      this.explodeMaximumSelfDamage = this.register((new Setting("Explode Maximum Self Damage", 8.0F, 0.0F, 36.0F)).setParent(this.explodingParent));
      this.explodeAntiSuicide = this.register((new Setting("Explode Anti Suicide", false)).setParent(this.explodingParent));
      this.explodePacket = this.register((new Setting("Explode Packet", false)).setParent(this.explodingParent));
      this.explodeSwing = this.register((new Setting("Explode Swing", false)).setParent(this.explodingParent));
      this.explodeSwingHand = this.register((new Setting("Explode Swing Offhand", SwingType.MainHand, (v) -> {
         return (Boolean)this.explodeSwing.getValue();
      })).setParent(this.explodingParent));
      this.facePlacingParent = this.registerParent(new ParentSetting("FacePlacing"));
      this.facePlaceHp = this.register((new Setting("Face Place HP", 10.0F, 0.0F, 36.0F)).setParent(this.facePlacingParent));
      this.predictingParent = this.registerParent(new ParentSetting("Predicting"));
      this.predict = this.register((new Setting("Predict", false)).setParent(this.predictingParent));
      this.predictDelay = this.register((new Setting("Predict Delay", 60, 0, 500, (v) -> {
         return (Boolean)this.predict.getValue();
      })).setParent(this.predictingParent));
      this.predictSetDead = this.register((new Setting("Predict Set Dead", false, (v) -> {
         return (Boolean)this.predict.getValue();
      })).setParent(this.predictingParent));
      this.rendering = this.registerParent(new ParentSetting("Rendering"));
      this.renderType = this.register((new Setting("Render Type", AutoCrystalRewrite.RenderType.Place)).setParent(this.rendering));
      this.placeBox = this.register((new Setting("Place Box", false, (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Place) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both);
      })).setParent(this.rendering));
      this.placeBoxColor = this.register((new Setting("Place Box Color", new Color(16777215), (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Place) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both) && (Boolean)this.placeBox.getValue();
      })).setParent(this.rendering));
      this.placeOutline = this.register((new Setting("Place Outline", false, (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Place) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both);
      })).setParent(this.rendering));
      this.placeOutlineColor = this.register((new Setting("Place Outline Color", new Color(16777215), (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Place) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both) && (Boolean)this.placeOutline.getValue();
      })).setParent(this.rendering));
      this.placeLineWidth = this.register((new Setting("Place Line Width", 1.0F, 0.0F, 5.0F, (v) -> {
         return (Boolean)this.placeOutline.getValue();
      })).setParent(this.rendering));
      this.placeText = this.register((new Setting("Place Text", false, (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Place) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both);
      })).setParent(this.rendering));
      this.explodeBox = this.register((new Setting("Explode Box", false, (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Explode) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both);
      })).setParent(this.rendering));
      this.explodeBoxColor = this.register((new Setting("Explode Box Color", new Color(16777215), (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Explode) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both) && (Boolean)this.explodeBox.getValue();
      })).setParent(this.rendering));
      this.explodeOutline = this.register((new Setting("Explode Outline", false, (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Explode) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both);
      })).setParent(this.rendering));
      this.explodeOutlineColor = this.register((new Setting("Explode Outline Color", new Color(16777215), (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Explode) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both) && (Boolean)this.explodeOutline.getValue();
      })).setParent(this.rendering));
      this.explodeLineWidth = this.register((new Setting("Explode Line Width", 1.0F, 0.0F, 5.0F, (v) -> {
         return (Boolean)this.explodeOutline.getValue();
      })).setParent(this.rendering));
      this.explodeText = this.register((new Setting("Explode Text", false, (v) -> {
         return ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Explode) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both);
      })).setParent(this.rendering));
      this.placePosition = new AutoCrystalRewrite.PlacePosition((BlockPos)null, 0.0F);
      this.explodePosition = new AutoCrystalRewrite.ExplodePosition((Entity)null, 0.0F);
      this.placeTimer = new Timer();
      this.explodeTimer = new Timer();
      this.predictTimer = new Timer();
      this.setInstance();
   }

   public static AutoCrystalRewrite getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new AutoCrystalRewrite();
      }

      return INSTANCE;
   }

   void setInstance() {
      INSTANCE = this;
   }

   public void onUpdate() {
      this.setup();
      if (this.target != null) {
         if (this.placePosition != null && this.placeTimer.passedMs((long)(Integer)this.placeDelay.getValue())) {
            this.placeCrystal();
         }

         if (this.explodePosition != null && this.explodeTimer.passedMs((long)(Integer)this.breakDelay.getValue())) {
            this.explodeCrystal();
         }

      }
   }

   public void setup() {
      if (!fullNullCheck()) {
         this.target = EntityUtil.getTarget((Float)this.targetRange.getValue());
         if (this.target != null) {
            this.placePosition = this.searchPosition();
            this.explodePosition = this.searchCrystal();
         }
      }
   }

   public void explodeCrystal() {
      if ((Boolean)this.explodePacket.getValue()) {
         ((NetHandlerPlayClient)Objects.requireNonNull(mc.func_147114_u())).func_147297_a(new CPacketUseEntity(this.explodePosition.getEntity()));
      } else {
         mc.field_71442_b.func_78764_a(mc.field_71439_g, this.explodePosition.getEntity());
      }

      if ((Boolean)this.explodeSwing.getValue()) {
         EntityUtil.swingArm((SwingType)this.explodeSwingHand.getValue());
      }

      this.explodeTimer.reset();
      this.currentThread = AutoCrystalRewrite.CurrentThread.Exploding;
   }

   public void placeCrystal() {
      if ((Boolean)this.placePacket.getValue()) {
         ((NetHandlerPlayClient)Objects.requireNonNull(mc.func_147114_u())).func_147297_a(new CPacketPlayerTryUseItemOnBlock(this.placePosition.getBlockPos(), EnumFacing.UP, mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.5F, 0.5F, 0.5F));
      } else {
         mc.field_71442_b.func_187099_a(mc.field_71439_g, mc.field_71441_e, this.placePosition.getBlockPos(), EnumFacing.UP, new Vec3d(mc.field_71439_g.field_70165_t, -mc.field_71439_g.field_70163_u, -mc.field_71439_g.field_70161_v), mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_185158_cP) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
      }

      if ((Boolean)this.placeSwing.getValue()) {
         EntityUtil.swingArm((SwingType)this.placeSwingHand.getValue());
      }

      this.placeTimer.reset();
      this.currentThread = AutoCrystalRewrite.CurrentThread.Placing;
   }

   public AutoCrystalRewrite.PlacePosition searchPosition() {
      TreeMap<Float, AutoCrystalRewrite.PlacePosition> posList = new TreeMap();
      Iterator var2 = BlockUtil.getSphereAutoCrystal((double)(Float)this.placeRange.getValue(), true).iterator();

      while(true) {
         BlockPos pos;
         float targetDamage;
         float selfDamage;
         float selfHealth;
         do {
            float minimumDamageValue;
            do {
               do {
                  float targetHealth;
                  do {
                     do {
                        do {
                           if (!var2.hasNext()) {
                              if (!posList.isEmpty()) {
                                 return (AutoCrystalRewrite.PlacePosition)posList.lastEntry().getValue();
                              }

                              return null;
                           }

                           pos = (BlockPos)var2.next();
                        } while(!BlockUtil.isPosValidForCrystal(pos, false));

                        targetDamage = EntityUtil.calculatePosDamage(pos, this.target);
                        selfDamage = EntityUtil.calculatePosDamage(pos, mc.field_71439_g);
                        selfHealth = mc.field_71439_g.func_110143_aJ() + mc.field_71439_g.func_110139_bj();
                        targetHealth = this.target.func_110143_aJ() + this.target.func_110139_bj();
                        minimumDamageValue = (Float)this.placeMinimumDamage.getValue();
                     } while(mc.field_71439_g.func_70092_e((double)((float)pos.func_177958_n() + 0.5F), (double)pos.func_177956_o(), (double)((float)pos.func_177952_p() + 0.5F)) > MathUtil.square((double)(Float)this.placeRange.getValue()));
                  } while(BlockUtil.rayTraceCheckPos(new BlockPos(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p())) && mc.field_71439_g.func_70011_f((double)((float)pos.func_177958_n() + 0.5F), (double)(pos.func_177956_o() + 1), (double)((float)pos.func_177952_p() + 0.5F)) > (double)(Float)this.placeWallRange.getValue());

                  if (BlockUtil.isPlayerSafe(this.target) && targetHealth < (Float)this.facePlaceHp.getValue()) {
                     minimumDamageValue = 2.0F;
                  }
               } while(targetDamage < minimumDamageValue);
            } while(selfDamage > (Float)this.placeMaximumSelfDamage.getValue());
         } while((Boolean)this.placeAntiSuicide.getValue() && selfDamage > selfHealth);

         posList.put(targetDamage, new AutoCrystalRewrite.PlacePosition(pos, targetDamage));
         this.currentThread = AutoCrystalRewrite.CurrentThread.Calculating;
      }
   }

   public AutoCrystalRewrite.ExplodePosition searchCrystal() {
      TreeMap<Float, AutoCrystalRewrite.ExplodePosition> crystalList = new TreeMap();
      Iterator var2 = mc.field_71441_e.field_72996_f.iterator();

      while(true) {
         Entity entity;
         float selfHealth;
         float selfDamage;
         float targetDamage;
         do {
            float minimumDamageValue;
            do {
               do {
                  float targetHealth;
                  do {
                     do {
                        do {
                           if (!var2.hasNext()) {
                              if (!crystalList.isEmpty()) {
                                 return (AutoCrystalRewrite.ExplodePosition)crystalList.lastEntry().getValue();
                              }

                              return null;
                           }

                           entity = (Entity)var2.next();
                        } while(!(entity instanceof EntityEnderCrystal));

                        selfHealth = mc.field_71439_g.func_110143_aJ() + mc.field_71439_g.func_110139_bj();
                        selfDamage = EntityUtil.calculateEntityDamage((EntityEnderCrystal)entity, mc.field_71439_g);
                        targetDamage = EntityUtil.calculateEntityDamage((EntityEnderCrystal)entity, this.target);
                        targetHealth = this.target.func_110143_aJ() + this.target.func_110139_bj();
                        minimumDamageValue = (Float)this.explodeMinimumDamage.getValue();
                     } while(entity.func_174818_b(EntityUtil.getPlayerPos(mc.field_71439_g)) > MathUtil.square((double)(Float)this.explodeRange.getValue()));
                  } while(BlockUtil.rayTraceCheckPos(new BlockPos(Math.floor(entity.field_70165_t), Math.floor(entity.field_70163_u), Math.floor(entity.field_70161_v))) && mc.field_71439_g.func_174818_b(new BlockPos(Math.floor(entity.field_70165_t), Math.floor(entity.field_70163_u), Math.floor(entity.field_70161_v))) > (double)(Float)this.explodeWallRange.getValue());

                  if (BlockUtil.isPlayerSafe(this.target) && targetHealth < (Float)this.facePlaceHp.getValue()) {
                     minimumDamageValue = 2.0F;
                  }
               } while(targetDamage < minimumDamageValue);
            } while(selfDamage > (Float)this.explodeMaximumSelfDamage.getValue());
         } while((Boolean)this.explodeAntiSuicide.getValue() && selfDamage > selfHealth);

         crystalList.put(targetDamage, new AutoCrystalRewrite.ExplodePosition(entity, targetDamage));
         this.currentThread = AutoCrystalRewrite.CurrentThread.Calculating;
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onPacketReceive(Receive event) {
      if (!this.megaNullCheck()) {
         if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject)event.getPacket()).func_148993_l() == 51 && (Boolean)this.predict.getValue() && this.target != null) {
            CPacketUseEntity predict = new CPacketUseEntity();
            predict.field_149567_a = ((SPacketSpawnObject)event.getPacket()).func_149001_c();
            predict.field_149566_b = Action.ATTACK;
            mc.field_71439_g.field_71174_a.func_147297_a(new CPacketAnimation(EnumHand.MAIN_HAND));
            mc.field_71439_g.field_71174_a.func_147297_a(predict);
         }

         if (event.getPacket() instanceof SPacketSoundEffect && (Boolean)this.predict.getValue() && (Boolean)this.predictSetDead.getValue()) {
            SPacketSoundEffect packet = (SPacketSoundEffect)event.getPacket();

            try {
               if (packet.func_186977_b() == SoundCategory.BLOCKS && packet.func_186978_a() == SoundEvents.field_187539_bB) {
                  List<Entity> loadedEntityList = mc.field_71441_e.field_72996_f;
                  loadedEntityList.stream().filter((entity) -> {
                     return entity instanceof EntityEnderCrystal && entity.func_70092_e(packet.func_149207_d(), packet.func_149211_e(), packet.func_149210_f()) < MathUtil.square((double)(Float)this.explodeRange.getValue());
                  }).forEach((entity) -> {
                     ((Entity)Objects.requireNonNull(mc.field_71441_e.func_73045_a(entity.func_145782_y()))).func_70106_y();
                     mc.field_71441_e.func_73028_b(entity.field_145783_c);
                  });
               }
            } catch (Exception var5) {
            }
         }

         if (event.getPacket() instanceof SPacketExplosion && (Boolean)this.predict.getValue() && (Boolean)this.predictSetDead.getValue()) {
            try {
               SPacketExplosion packet = (SPacketExplosion)event.getPacket();
               mc.field_71441_e.field_72996_f.stream().filter((entity) -> {
                  return entity instanceof EntityEnderCrystal && entity.func_70092_e(packet.func_149148_f(), packet.func_149143_g(), packet.func_149145_h()) < MathUtil.square((double)(Float)this.explodeRange.getValue());
               }).forEach((entity) -> {
                  ((Entity)Objects.requireNonNull(mc.field_71441_e.func_73045_a(entity.func_145782_y()))).func_70106_y();
                  mc.field_71441_e.func_73028_b(entity.field_145783_c);
               });
            } catch (Exception var4) {
            }
         }

      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onPacketSend(Send event) {
      if (!this.megaNullCheck()) {
         if (event.getPacket() instanceof CPacketUseEntity && (Boolean)this.predict.getValue() && this.predictTimer.passedMs((long)(Integer)this.predictDelay.getValue())) {
            CPacketUseEntity packet = (CPacketUseEntity)event.getPacket();
            if (packet.func_149565_c() == Action.ATTACK && packet.func_149564_a(mc.field_71441_e) instanceof EntityEnderCrystal) {
               if ((Boolean)this.predictSetDead.getValue()) {
                  ((Entity)Objects.requireNonNull(packet.func_149564_a(mc.field_71441_e))).func_70106_y();
                  mc.field_71441_e.func_73028_b(packet.field_149567_a);
               }

               if (this.placePosition != null) {
                  this.placeCrystal();
                  this.currentThread = AutoCrystalRewrite.CurrentThread.Placing;
               }

               this.predictTimer.reset();
            }
         }

      }
   }

   public void onRender3D(Render3DEvent event) {
      if (this.target != null) {
         double damage;
         if (this.placePosition != null && (((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Place) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both))) {
            RenderUtil.drawBoxESP(this.placePosition.getBlockPos(), (Color)this.placeBoxColor.getValue(), true, (Color)this.placeOutlineColor.getValue(), (Float)this.placeLineWidth.getValue(), (Boolean)this.placeOutline.getValue(), (Boolean)this.placeBox.getValue(), ((Color)this.placeBoxColor.getValue()).getAlpha(), true);
            damage = (double)EntityUtil.calculatePosDamage((double)this.placePosition.getBlockPos().func_177958_n() + 0.5D, (double)this.placePosition.getBlockPos().func_177956_o() + 1.0D, (double)this.placePosition.getBlockPos().func_177952_p() + 0.5D, this.target);
            if ((Boolean)this.placeText.getValue()) {
               RenderUtil.drawText(this.placePosition.getBlockPos(), (Math.floor(damage) == damage ? (int)damage : String.format("%.1f", damage)) + "");
            }
         }

         if (this.explodePosition != null && (((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Explode) || ((AutoCrystalRewrite.RenderType)this.renderType.getValue()).equals(AutoCrystalRewrite.RenderType.Both))) {
            RenderUtil.drawBoxESP(this.explodePosition.getEntity().func_180425_c(), (Color)this.explodeBoxColor.getValue(), true, (Color)this.explodeOutlineColor.getValue(), (Float)this.explodeLineWidth.getValue(), (Boolean)this.explodeOutline.getValue(), (Boolean)this.explodeBox.getValue(), ((Color)this.explodeBoxColor.getValue()).getAlpha(), true);
            damage = (double)EntityUtil.calculatePosDamage(Math.floor((double)this.explodePosition.getEntity().func_180425_c().func_177958_n()), Math.floor((double)this.explodePosition.getEntity().func_180425_c().func_177956_o()), Math.floor((double)this.explodePosition.getEntity().func_180425_c().func_177952_p()), this.target);
            if ((Boolean)this.explodeText.getValue()) {
               RenderUtil.drawText(this.explodePosition.getEntity().func_180425_c(), (Math.floor(damage) == damage ? (int)damage : String.format("%.1f", damage)) + "");
            }
         }

      }
   }

   public String getDisplayInfo() {
      return this.currentThread == null ? "" : this.currentThread.toString();
   }

   public static class PlacePosition {
      BlockPos blockPos;
      float targetDamage;

      public PlacePosition(BlockPos blockPos, float targetDamage) {
         this.blockPos = blockPos;
         this.targetDamage = targetDamage;
      }

      public BlockPos getBlockPos() {
         return this.blockPos;
      }
   }

   public static class ExplodePosition {
      Entity entity;
      float targetDamage;

      public ExplodePosition(Entity entity, float targetDamage) {
         this.entity = entity;
         this.targetDamage = targetDamage;
      }

      public Entity getEntity() {
         return this.entity;
      }
   }

   public static enum CurrentThread {
      Placing,
      Exploding,
      Calculating;
   }

   public static enum RenderType {
      Place,
      Explode,
      Both;
   }
}
