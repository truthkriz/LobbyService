package eu.kartoffelquadrat.ls.guest.api;

import eu.kartoffelquadrat.ls.guest.service.GuestLobbyService;
import eu.kartoffelquadrat.ls.guest.service.GuestLobbyService.Player;
import eu.kartoffelquadrat.ls.guest.service.GuestLobbyService.Room;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/guest")
@Profile("guest")
public class GuestLobbyController {

  private final GuestLobbyService service;
  public GuestLobbyController(GuestLobbyService service) { this.service = service; }

  public static class NameReq { public String name; }
  public static class CreateRoomRes {
    public String code; public String hostId;
    public CreateRoomRes(String code, String hostId){this.code=code;this.hostId=hostId;}
  }
  public static class JoinRes { public String playerId; public JoinRes(String id){this.playerId=id;} }
  public static class RoomRes {
    public String code; public String hostId;
    public List<Map<String,Object>> players;
    public RoomRes(Room r){
      this.code=r.code; this.hostId=r.hostId;
      this.players = r.players.values().stream().map(p -> {
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("id", p.id); m.put("name", p.name); m.put("host", p.host);
        return m;
      }).collect(Collectors.toList());
    }
  }

  @PostMapping("/rooms")
  public ResponseEntity<?> create(@RequestBody NameReq req){
    if (req==null || req.name==null || req.name.trim().isEmpty())
      return ResponseEntity.badRequest().body(Map.of("error","name required"));
    Room r = service.createRoom(req.name.trim());
    return ResponseEntity.ok(new CreateRoomRes(r.code, r.hostId));
  }

  @PostMapping("/rooms/{code}/join")
  public ResponseEntity<?> join(@PathVariable String code, @RequestBody NameReq req){
    if (req==null || req.name==null || req.name.trim().isEmpty())
      return ResponseEntity.badRequest().body(Map.of("error","name required"));
    try {
      Player p = service.join(code, req.name.trim());
      return ResponseEntity.ok(new JoinRes(p.id));
    } catch (NoSuchElementException e){
      return ResponseEntity.status(404).body(Map.of("error","room not found"));
    }
  }

  @GetMapping("/rooms/{code}")
  public ResponseEntity<?> room(@PathVariable String code){
    try {
      return ResponseEntity.ok(new RoomRes(service.get(code)));
    } catch (NoSuchElementException e){
      return ResponseEntity.status(404).body(Map.of("error","room not found"));
    }
  }
}
