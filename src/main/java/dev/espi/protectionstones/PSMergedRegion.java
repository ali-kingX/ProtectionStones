/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.espi.protectionstones;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.espi.protectionstones.event.PSRemoveEvent;
import dev.espi.protectionstones.utils.WGMerge;
import dev.espi.protectionstones.utils.WGUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an instance of a PS region that has been merged into another region. There is no actual WG region that
 * this contains, and instead takes properties from its parent region (see {@link PSGroupRegion}).
 */

public class PSMergedRegion extends PSRegion {

    private PSGroupRegion mergedGroup;
    private String id, type;

    PSMergedRegion(String id, PSGroupRegion mergedGroup, RegionManager rgmanager, World world) {
        super(rgmanager, world);
        this.id = id;
        this.mergedGroup = mergedGroup;

        // get type
        // stored instead of fetched on the fly because unmerge algorithm removes the flag causing getType to return null
        for (String s : mergedGroup.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES)) {
            String[] spl = s.split(" ");
            String did = spl[0], type = spl[1];
            if (did.equals(getID())) {
                this.type = type;
                break;
            }
        }
    }

    // ~~~~~~~~~~~ static ~~~~~~~~~~~~~~~~

    /**
     * Finds the {@link PSMergedRegion} at a location that is a part of a merged region.
     *
     * @param l location to look at
     * @return the {@link PSMergedRegion} the location is in, or null if not applicable
     */
    public static PSMergedRegion getMergedRegion(Location l) {
        String psID = WGUtils.createPSID(l);
        RegionManager rgm = WGUtils.getRegionManagerWithWorld(l.getWorld());
        if (rgm == null) return null;

        for (ProtectedRegion pr : rgm.getApplicableRegions(BlockVector3.at(l.getX(), l.getY(), l.getZ()))) {
            if (pr.getFlag(FlagHandler.PS_MERGED_REGIONS) != null && pr.getFlag(FlagHandler.PS_MERGED_REGIONS).contains(psID)) {
                for (String s : pr.getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES)) {
                    String[] spl = s.split(" ");
                    String id = spl[0], type = spl[1];
                    if (id.equals(psID)) {
                        return new PSMergedRegion(psID, new PSGroupRegion(pr, rgm, l.getWorld()), rgm, l.getWorld());
                    }
                }
            }
        }

        return null;
    }

    // ~~~~~~~~~~~ instance ~~~~~~~~~~~~~~~~

    /**
     * Get the group region that contains this region.
     *
     * @return the group region
     */
    public PSGroupRegion getGroupRegion() {
        return mergedGroup;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getName() {
        return mergedGroup.getName();
    }

    @Override
    public void setName(String name) {
        mergedGroup.setName(name);
    }

    @Override
    public void setParent(PSRegion r) throws ProtectedRegion.CircularInheritanceException {
        mergedGroup.setParent(r);
    }

    @Override
    public PSRegion getParent() {
        return mergedGroup.getParent();
    }

    @Override
    public Location getHome() {
        return mergedGroup.getHome();
    }

    @Override
    public void setHome(double blockX, double blockY, double blockZ) {
        mergedGroup.setHome(blockX, blockY, blockZ);
    }

    @Override
    public boolean forSale() {
        return mergedGroup.forSale();
    }

    @Override
    public void setSellable(boolean forSale, UUID landlord, double price) {
        mergedGroup.setSellable(forSale, landlord, price);
    }

    @Override
    public void sell(UUID player) {
        mergedGroup.sell(player);
    }

    @Override
    public RentStage getRentStage() {
        return mergedGroup.getRentStage();
    }

    @Override
    public UUID getLandlord() {
        return mergedGroup.getLandlord();
    }

    @Override
    public void setLandlord(UUID landlord) {
        mergedGroup.setLandlord(landlord);
    }

    @Override
    public UUID getTenant() {
        return mergedGroup.getTenant();
    }

    @Override
    public void setTenant(UUID tenant) {
        mergedGroup.setTenant(tenant);
    }

    @Override
    public String getRentPeriod() {
        return mergedGroup.getRentPeriod();
    }

    @Override
    public void setRentPeriod(String s) {
        mergedGroup.setRentPeriod(s);
    }

    @Override
    public Double getPrice() {
        return mergedGroup.getPrice();
    }

    @Override
    public void setPrice(Double price) {
        mergedGroup.setPrice(price);
    }

    @Override
    public void setRentLastPaid(Long timestamp) {
        mergedGroup.setRentLastPaid(timestamp);
    }

    @Override
    public Long getRentLastPaid() {
        return mergedGroup.getRentLastPaid();
    }

    @Override
    public void setRentable(UUID landlord, String rentPeriod, double rentPrice) {
        mergedGroup.setRentable(landlord, rentPeriod, rentPrice);
    }

    @Override
    public void rentOut(UUID landlord, UUID tenant, String rentPeriod, double rentPrice) {
        mergedGroup.rentOut(landlord, tenant, rentPeriod, rentPrice);
    }

    @Override
    public void removeRenting() {
        mergedGroup.removeRenting();
    }

    @Override
    public List<TaxPayment> getTaxPaymentsDue() {
        return mergedGroup.getTaxPaymentsDue();
    }

    @Override
    public UUID getTaxAutopayer() {
        return mergedGroup.getTaxAutopayer();
    }

    @Override
    public void setTaxAutopayer(UUID uuid) {
        mergedGroup.setTaxAutopayer(uuid);
    }

    @Override
    public EconomyResponse payTax(PSPlayer p, double amount) {
        return mergedGroup.payTax(p, amount);
    }

    @Override
    public boolean isTaxPaymentLate() {
        return mergedGroup.isTaxPaymentLate();
    }

    @Override
    public void updateTaxPayments() {
        mergedGroup.updateTaxPayments();
    }

    @Override
    public Block getProtectBlock() {
        PSLocation psl = WGUtils.parsePSRegionToLocation(id);
        return world.getBlockAt(psl.x, psl.y, psl.z);
    }

    @Override
    public PSProtectBlock getTypeOptions() {
        return ProtectionStones.getBlockOptions(getType());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(PSProtectBlock type) {

        super.setType(type);

        // has to be after isHidden query
        this.type = type.type;

        Set<String> flag = mergedGroup.getWGRegion().getFlag(FlagHandler.PS_MERGED_REGIONS_TYPES);
        String original = null;
        for (String s : flag) {
            String[] spl = s.split(" ");
            String id = spl[0];
            if (id.equals(getID())) {
                original = s;
                break;
            }
        }

        if (original != null) {
            flag.remove(original);
            flag.add(getID() + " " + type.type);
        }
    }

    @Override
    public boolean isOwner(UUID uuid) {
        return mergedGroup.isOwner(uuid);
    }

    @Override
    public boolean isMember(UUID uuid) {
        return mergedGroup.isMember(uuid);
    }

    @Override
    public ArrayList<UUID> getOwners() {
        return mergedGroup.getOwners();
    }

    @Override
    public ArrayList<UUID> getMembers() {
        return mergedGroup.getMembers();
    }

    @Override
    public List<BlockVector2> getPoints() {
        return WGUtils.getDefaultProtectedRegion(getTypeOptions(), WGUtils.parsePSRegionToLocation(id)).getPoints();
    }

    @Override
    public List<PSRegion> getMergeableRegions(Player p) {
        return mergedGroup.getMergeableRegions(p);
    }

    @Override
    public boolean deleteRegion(boolean deleteBlock) {
        return deleteRegion(deleteBlock, null);
    }

    @Override
    public boolean deleteRegion(boolean deleteBlock, Player cause) {
        PSRemoveEvent event = new PSRemoveEvent(this, cause);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { // if event was cancelled, prevent execution
            return false;
        }

        if (deleteBlock && !this.isHidden()) {
            this.getProtectBlock().setType(Material.AIR);
        }

        try {
            WGMerge.unmergeRegion(getWorld(), getWGRegionManager(), this);
        } catch (WGMerge.RegionHoleException | WGMerge.RegionCannotMergeWhileRentedException e) {
            this.unhide();
            return false;
        }

        return true;
    }

    @Override
    public ProtectedRegion getWGRegion() {
        return WGUtils.getDefaultProtectedRegion(getTypeOptions(), WGUtils.parsePSRegionToLocation(id));
    }
}
