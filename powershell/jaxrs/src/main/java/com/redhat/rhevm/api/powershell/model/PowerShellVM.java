/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.powershell.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.CpuStatistics;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.MemoryStatistics;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VmStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellBootSequence;
import com.redhat.rhevm.api.powershell.enums.PowerShellVmType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.UUID;

public class PowerShellVM extends VM {

    private static final String VM_TYPE = "RhevmCmd.CLIVm";
    private static final String MEMORY_STATS_TYPE = "RhevmCmd.MemoryStatistics";
    private static final String CPU_STATS_TYPE = "RhevmCmd.CLICpu";

    private String cdIsoPath;
    public String getCdIsoPath() {
        return cdIsoPath;
    }
    public void setCdIsoPath(String cdIsoPath) {
        this.cdIsoPath = cdIsoPath;
    }

    public static String buildBootSequence(OperatingSystem os) {
        if (os == null || os.getBoot().size() <= 0) {
            return null;
        }
        String bootSequence = "";
        for (OperatingSystem.Boot boot : os.getBoot()) {
            if (boot.getDev() == null) {
                continue;
            }
            switch (boot.getDev()) {
            case HD:
                bootSequence += "C";
                break;
            case CDROM:
                bootSequence += "D";
                break;
            case NETWORK:
                bootSequence += "N";
                break;
            default:
                break;
            }
        }
        return !bootSequence.isEmpty() ? bootSequence : null;
    }

    public static String asString(DisplayType type) {
        return DisplayType.VNC.equals(type) ? "VNC" : "Spice";
    }

    private static DisplayType parseDisplayType(String s) {
        if (s == null) return null;
        else if (s.equals("VNC"))   return DisplayType.VNC;
        else if (s.equals("Spice")) return DisplayType.SPICE;
        else return null;
    }

    private static VmStatus parseStatus(String s) {
        if (s == null) return null;
        else if (s.equals("Unassigned"))       return VmStatus.UNASSIGNED;
        else if (s.equals("Down"))             return VmStatus.DOWN;
        else if (s.equals("Up"))               return VmStatus.UP;
        else if (s.equals("PoweringUp"))       return VmStatus.POWERING_UP;
        else if (s.equals("PoweredDown"))      return VmStatus.POWERED_DOWN;
        else if (s.equals("Paused"))           return VmStatus.PAUSED;
        else if (s.equals("MigratingFrom"))    return VmStatus.MIGRATING_FROM;
        else if (s.equals("MigratingTo"))      return VmStatus.MIGRATING_TO;
        else if (s.equals("Unknown"))          return VmStatus.UNKNOWN;
        else if (s.equals("NotResponding"))    return VmStatus.NOT_RESPONDING;
        else if (s.equals("WaitForLaunch"))    return VmStatus.WAIT_FOR_LAUNCH;
        else if (s.equals("RebootInProgress")) return VmStatus.REBOOT_IN_PROGRESS;
        else if (s.equals("SavingState"))      return VmStatus.SAVING_STATE;
        else if (s.equals("RestoringState"))   return VmStatus.RESTORING_STATE;
        else if (s.equals("Suspended"))        return VmStatus.SUSPENDED;
        else if (s.equals("ImageIllegal"))     return VmStatus.IMAGE_ILLEGAL;
        else if (s.equals("ImageLocked"))      return VmStatus.IMAGE_LOCKED;
        else if (s.equals("PoweringDown"))     return VmStatus.POWERING_DOWN;
        else return null;
    }

    public static List<PowerShellVM> parse(PowerShellParser parser, String output) {
        List<PowerShellVM> ret = new ArrayList<PowerShellVM>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (VM_TYPE.equals(entity.getType())) {
                ret.add(parseVm(entity));
            } else if (MEMORY_STATS_TYPE.equals(entity.getType())) {
                parseMemoryStats(entity, last(ret));
            } else if (CPU_STATS_TYPE.equals(entity.getType())) {
                parseCpuStats(entity, last(ret));
            }
        }

        return ret;
    }

    private static PowerShellVM last(List<PowerShellVM> list) {
        return list.get(list.size() - 1);
    }

    private static PowerShellVM parseVm(PowerShellParser.Entity entity) {
        PowerShellVM vm = new PowerShellVM();

        vm.setId(entity.get("vmid"));
        vm.setName(entity.get("name"));
        vm.setDescription(entity.get("description"));
        vm.setType(entity.get("vmtype", PowerShellVmType.class).map());
        vm.setMemory(entity.get("memorysize", Integer.class) * 1024L * 1024L);
        vm.setCdIsoPath(entity.get("cdisopath"));

        VmStatus status = parseStatus(entity.get("status"));
        if (status != null) {
            vm.setStatus(status);
        }

        CpuTopology topo = new CpuTopology();
        topo.setSockets(entity.get("numofsockets", Integer.class));
        topo.setCores(entity.get("numofcpuspersocket", Integer.class));
        CPU cpu = new CPU();
        cpu.setTopology(topo);
        vm.setCpu(cpu);

        OperatingSystem os = new OperatingSystem();
        for (OperatingSystem.Boot boot : entity.get("defaultbootsequence", PowerShellBootSequence.class).map()) {
            os.getBoot().add(boot);
        }
        vm.setOs(os);

        Object hostId = entity.get("runningonhost", String.class, Integer.class);
        if (!isEmptyId(hostId)) {
            Host host = new Host();
            host.setId(hostId.toString());
            vm.setHost(host);
        }

        Cluster cluster = new Cluster();
        cluster.setId(entity.get("hostclusterid", String.class, Integer.class).toString());
        vm.setCluster(cluster);

        Template template = new Template();
        template.setId(entity.get("templateid"));
        vm.setTemplate(template);

        Object poolId = entity.get("poolid", String.class, Integer.class);
        if (!isEmptyId(poolId)) {
            VmPool pool = new VmPool();
            pool.setId(poolId.toString());
            vm.setVmPool(pool);
        }

        DisplayType displayType = parseDisplayType(entity.get("displaytype"));
        if (displayType != null) {
            Display display = new Display();
            display.setType(displayType);
            display.setMonitors(entity.get("numofmonitors", Integer.class));
            int port = entity.get("displayport", Integer.class);
            if (port != -1) {
                display.setPort(port);
            }
            vm.setDisplay(display);
        }
        return vm;
    }

    private static void parseMemoryStats(PowerShellParser.Entity entity, PowerShellVM vm) {
        if (!vm.isSetMemoryStatistics()) {
            vm.setMemoryStatistics(new MemoryStatistics());
        }
        vm.getMemoryStatistics().setUtilization((long)entity.get("usagemempercent", Integer.class));
    }

    private static void parseCpuStats(PowerShellParser.Entity entity, PowerShellVM vm) {
        if (!vm.isSetCpuStatistics()) {
            vm.setCpuStatistics(new CpuStatistics());
        }
        vm.getCpuStatistics().setUser(entity.get("user", BigDecimal.class));
        vm.getCpuStatistics().setSystem(entity.get("system", BigDecimal.class));
        vm.getCpuStatistics().setIdle(entity.get("idle", BigDecimal.class));
        vm.getCpuStatistics().setLoad(entity.get("load", BigDecimal.class));
    }

    private static boolean isEmptyId(Object id) {
        return id instanceof String && id.equals(UUID.EMPTY)
               || id instanceof Integer && id.equals(-1);
    }
}
