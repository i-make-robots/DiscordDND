package com.marginallyclever.discorddnd;

import com.formdev.flatlaf.FlatLightLaf;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DiscordDNDGUI extends JPanel {
    private final DiscordDND discordDND = new DiscordDND();
    private final DefaultListModel<String> queueModel = new DefaultListModel<>();
    private final JList<String> trackList = new JList<>(queueModel);
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final ImageIcon playIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever.discordnd/icons8-play-16.png")));
    private final ImageIcon pauseIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever.discordnd/icons8-pause-16.png")));
    private final ImageIcon loopIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever.discordnd/icons8-loop-16.png")));
    private final JButton playButton = new JButton(playIcon);
    private final JProgressBar trackProgressBar = new JProgressBar();
    private final JSlider volumeSlider = new JSlider();
    private final JToggleButton loopAtEndButton = new JToggleButton(loopIcon);
    private int currentlyPlayingIndex = -1;
    GuildMusicManager musicManager;

    public static void main(String[] args) {
        FlatLightLaf.setup();

        JFrame frame = new JFrame("Discord DND");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.add(new DiscordDNDGUI());

        javax.swing.SwingUtilities.invokeLater(()->frame.setVisible(true));
    }

    public DiscordDNDGUI() {
        super(new BorderLayout());
        setupMusicManager();

        int padding = 4;

        var guildSelectionContainer = new JPanel(new BorderLayout(padding,padding));
        guildSelectionContainer.setBorder(new EmptyBorder(0, padding, 0, padding));
        add(guildSelectionContainer,BorderLayout.NORTH);

        var voiceChannelsContainer = new JPanel(new BorderLayout(padding,0));
        guildSelectionContainer.add(voiceChannelsContainer,BorderLayout.SOUTH);

        var audioSelectionContainer = new JPanel(new BorderLayout(padding,1));
        add(audioSelectionContainer,BorderLayout.CENTER);

        setupGuildSelection(guildSelectionContainer,voiceChannelsContainer);
        setupPlayerAndTrackList(audioSelectionContainer);
    }

    private void setupMusicManager() {
        var ytSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager();
        // This will trigger an OAuth flow, where you will be instructed to head to YouTube's OAuth page and input a code.
        // This is safe, as it only uses YouTube's official OAuth flow. No tokens are seen or stored by us.
        ytSourceManager.useOauth2(null, false);
        playerManager.registerSourceManager(ytSourceManager);
        AudioSourceManagers.registerRemoteSources(playerManager,
                // exclusion list
                com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        AudioSourceManagers.registerLocalSource(playerManager);
        musicManager = new GuildMusicManager(playerManager);

        playNextTrackWhenThisOneEnds();
    }

    private void playNextTrackWhenThisOneEnds() {
        musicManager.player.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if(endReason==AudioTrackEndReason.FINISHED) {
                    System.out.println("onTrackEnd: "+track.getInfo().title);
                    currentlyPlayingIndex++;
                    if(currentlyPlayingIndex<queueModel.size()) {
                        playTrackAtIndex(trackList,currentlyPlayingIndex);
                    } else if(loopAtEndButton.isSelected()) {
                        currentlyPlayingIndex = 0;
                        playTrackAtIndex(trackList, currentlyPlayingIndex);
                    }
                }
                trackList.repaint();
                super.onTrackEnd(player,track,endReason);
            }
        });
    }

    private void setupPlayerAndTrackList(JPanel audioSelectionContainer) {
        JToolBar bar = new JToolBar();
        audioSelectionContainer.add(bar,BorderLayout.NORTH);
        bar.add(loopAtEndButton);
        bar.add(playButton);
        bar.add(volumeSlider);

        setupPlayButton();
        setupVolumeSlider();
        setupQueueDisplay();
        audioSelectionContainer.add(new JScrollPane(trackList), BorderLayout.CENTER);
        setupTrackProgressBar();
        audioSelectionContainer.add(trackProgressBar, BorderLayout.SOUTH);
    }

    private void setupTrackProgressBar() {
        trackProgressBar.setMinimumSize(new Dimension(100,20));
        trackProgressBar.setPreferredSize(new Dimension(100,20));
        trackProgressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                scrubToPosition(e, trackProgressBar);
            }
        });

        trackProgressBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                scrubToPosition(e, trackProgressBar);
            }
        });

        Timer progressTimer = new Timer(100, e -> {
            var track = musicManager.player.getPlayingTrack();
            if (track != null) {
                trackProgressBar.setMaximum((int) track.getDuration());
                trackProgressBar.setValue((int) track.getPosition());
                repaint();
            } else {
                trackProgressBar.setValue(0);
            }
        });

        progressTimer.start();
    }

    private void scrubToPosition(MouseEvent e, JProgressBar progressBar) {
        int mouseX = e.getX();
        int progressBarVal = (int) Math.round(((double) mouseX / (double) progressBar.getWidth()) * progressBar.getMaximum());
        musicManager.player.getPlayingTrack().setPosition(progressBarVal);
        progressBar.setValue(progressBarVal);
    }

    private void setupQueueDisplay() {
        trackList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        showCurrentTrackInGreen(trackList);
        doubleClickToPlaySelection(trackList);
        dragToRearrangeAndDropToQueue(trackList);
        deleteToRemoveSelectedItems(trackList);
    }

    private void setupVolumeSlider() {
        volumeSlider.addChangeListener(e->{
            System.out.println("Volume: "+volumeSlider.getValue());
            musicManager.player.setVolume(volumeSlider.getValue());
        });
    }

    private void setupPlayButton() {
        playButton.addActionListener(e->{
            musicManager.player.setPaused(!musicManager.player.isPaused());
            updatePlayButton();
        });
    }

    private void updatePlayButton() {
        if(musicManager.player.isPaused()) {
            playButton.setIcon(playIcon);
        } else {
            playButton.setIcon(pauseIcon);
        }
    }

    private void deleteToRemoveSelectedItems(JList<String> queueDisplay) {
        queueDisplay.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelectedTracks");
        queueDisplay.getActionMap().put("deleteSelectedTracks", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedTracks(queueDisplay);
            }
        });
    }

    private void playTrackAtIndex(JList<String> queueDisplay,int index) {
        currentlyPlayingIndex = index;
        String selectedTrack = queueModel.getElementAt(index);
        playerManager.loadItem(selectedTrack, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.player.playTrack(audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                musicManager.player.playTrack(firstTrack);
            }

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException e) {}
        });
        updatePlayButton();
        queueDisplay.repaint();
    }

    private void dragToRearrangeAndDropToQueue(JList<String> queueDisplay) {
        queueDisplay.setDragEnabled(true);
        queueDisplay.setDropMode(DropMode.INSERT);
        queueDisplay.setTransferHandler(new TransferHandler() {
            private int[] indices = null;
            private int addIndex = -1; // Location where items were added
            private int addCount = 0;  // Number of items added

            @Override
            protected Transferable createTransferable(JComponent c) {
                c.getRootPane().getGlassPane().setVisible(true);
                indices = queueDisplay.getSelectedIndices();
                var values = String.join("\n", queueDisplay.getSelectedValuesList());
                System.out.println("createTransferable: " + values);
                return new StringSelection(values);
            }

            @Override
            public int getSourceActions(JComponent c) {
                Component glassPane = c.getRootPane().getGlassPane();
                glassPane.setCursor(DragSource.DefaultMoveDrop);
                return MOVE; // COPY_OR_MOVE;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                boolean canImport = support.isDrop() &&
                        (support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
                         support.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
                System.out.println("canImport: " + canImport);
                return canImport;
            }

            @Override
            public boolean importData(TransferSupport support) {
                System.out.println("importData called");
                if (!canImport(support)) {
                    return false;
                }

                JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
                int index = dl.getIndex();
                boolean insert = dl.isInsert();

                try {
                    if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String data = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                        String[] values = data.split("\n");
                        addIndex = index;
                        addCount = values.length;

                        for (String value : values) {
                            if (insert) {
                                queueModel.add(index++, value);
                            } else {
                                queueModel.set(index++, value);
                            }
                        }
                    } else if(support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        java.util.List<File> files = (java.util.List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : files) {
                            queueModel.addElement(file.getAbsolutePath());
                        }
                    }
                    return true;
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
                return false;
            }

            @Override
            protected void exportDone(JComponent c, Transferable data, int action) {
                c.getRootPane().getGlassPane().setVisible(false);
                if (action == MOVE && indices != null) {
                    if (addCount > 0) {
                        for (int i = indices.length - 1; i >= 0; i--) {
                            if (indices[i] >= addIndex) {
                                indices[i] += addCount;
                            }
                        }
                    }
                    for (int i = indices.length - 1; i >= 0; i--) {
                        queueModel.remove(indices[i]);
                    }
                }
                indices = null;
                addCount = 0;
                addIndex = -1;
            }
        });
    }

    private void doubleClickToPlaySelection(JList<String> queueDisplay) {
        queueDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = queueDisplay.locationToIndex(e.getPoint());
                    if (index != -1) {
                        playTrackAtIndex(queueDisplay,index);
                    }
                }
            }
        });
    }

    private void showCurrentTrackInGreen(JList<String> queueDisplay) {
        queueDisplay.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index == currentlyPlayingIndex) {
                    c.setBackground(Color.GREEN);
                }
                return c;
            }
        });
    }

    private void deleteSelectedTracks(JList<String> queueDisplay) {
        int[] selectedIndices = queueDisplay.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            queueModel.remove(selectedIndices[i]);
            if(selectedIndices[i] == currentlyPlayingIndex) {
                // delete the current playing?  Stop it.
                musicManager.player.stopTrack();
                currentlyPlayingIndex = -1;
                trackProgressBar.setValue(0);
                updatePlayButton();
            }
        }
    }

    private void setupGuildSelection(JPanel guildSelectionContainer,JPanel voiceChannelsContainer) {
        guildSelectionContainer.add(new JLabel("Guild"), BorderLayout.WEST);

        var list = discordDND.getGuilds();
        list.add(0,"None");
        var guilds = new JComboBox<>(list.toArray(new String[0]));
        guildSelectionContainer.add(guilds, BorderLayout.CENTER);
        guilds.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // is e is the first item by index?
                if(guilds.getSelectedIndex()==0) {
                    discordDND.setGuild(null);
                    return;
                }
                System.out.println("Selected " + e.getItem());
                discordDND.setGuild((String) e.getItem());
                updateVoiceChannels(voiceChannelsContainer);
            }
        });
    }

    private void updateVoiceChannels(JPanel voiceChannelsContainer) {
        voiceChannelsContainer.removeAll();

        var list = discordDND.getVoiceChannels();
        list.add(0,"None");
        var voiceChannels = new JComboBox<>(list.toArray(new String[0]));
        voiceChannelsContainer.add(new JLabel("Voice Channel"),BorderLayout.WEST);
        voiceChannelsContainer.add(voiceChannels,BorderLayout.CENTER);

        voiceChannels.addItemListener(e->{
            if(e.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("Selected "+e.getItem());
                if(discordDND.joinVoiceChannel((String)e.getItem())) {
                    // success
                    discordDND.getAudioManager().setSendingHandler(musicManager.getSendHandler());
                };
            }
        });

        revalidate();
        repaint();
    }
}
